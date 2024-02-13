package com.tjut.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.shortlink.project.common.convention.exception.ClientException;
import com.tjut.shortlink.project.common.convention.exception.ServiceException;
import com.tjut.shortlink.project.common.enums.VailDateTypeEnum;
import com.tjut.shortlink.project.config.GotoDomainWhiteListConfiguration;
import com.tjut.shortlink.project.dao.entity.*;
import com.tjut.shortlink.project.dao.mapper.*;
import com.tjut.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.tjut.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.tjut.shortlink.project.dto.resp.*;
import com.tjut.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import com.tjut.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import com.tjut.shortlink.project.service.LinkService;
import com.tjut.shortlink.project.service.LinkStatsTodayService;
import com.tjut.shortlink.project.util.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.tjut.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.tjut.shortlink.project.util.HashUtil.hashToBase62;

/**
* @author a0000
* @description 针对表【t_link】的数据库操作Service实现
*/
@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class LinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO>
    implements LinkService {

    private final RBloomFilter<String> bloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkStatsTodayService linkStatsTodayService;
    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    /**
     * 开发环境默认域名测试
     */
    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;
    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;
//    @Value("${short-link.stats.locale.amap-key}")
//    private String statsLocaleAmapKey;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 验证
        verificationWhitelist(requestParam.getOriginUrl());
        String suffix = generateSuffix(requestParam);

//        String fullShortUrl = requestParam.getDomain() + "/" + suffix;
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(suffix)
                .toString();
        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setDomain(createShortLinkDefaultDomain);
        linkDO.setEnableStatus(0);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setShortUri(suffix);
        linkDO.setFavicon(getFavicon(requestParam.getOriginUrl()));
        linkDO.setTotalPv(0);
        linkDO.setTotalUip(0);
        linkDO.setTotalUv(0);
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            baseMapper.insert(linkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
        }
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS
        );
        bloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://"  + fullShortUrl)
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<LinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    public String generateSuffix(ShortLinkCreateReqDTO requestParam){
        int generate_count = 0;
        String originUrl = requestParam.getOriginUrl();
        String shortUri = null;
        while (true){
            if (generate_count > 10){
                throw new ServiceException("短链接重复次数过多");
            }
//            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
//                    .eq(LinkDO::getFullShortUrl, fullShortUrl);
//            LinkDO linkDO = baseMapper.selectOne(queryWrapper);
//            if (linkDO == null) break;
            originUrl += UUID.randomUUID().toString();
            shortUri = hashToBase62(originUrl);

            if (!bloomFilter.contains(createShortLinkDefaultDomain+ "/" + shortUri)) break;
            generate_count++;
        }
        return shortUri;
    }
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        // 批量查询每个短链接的数量
        QueryWrapper<LinkDO> queryWrapper = Wrappers.query(new LinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("del_flag", 0)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }


    /**
     * 更新短链接信息
     *
     * @param requestParam 包含更新所需参数的请求DTO
     */
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        verificationWhitelist(requestParam.getOriginUrl());

        // 1. 根据分组标识（gid）、完整短链接（fullShortUrl）、删除标识（delFlag）、启用状态（enableStatus）筛选数据库满足条件的数据
        LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getOriginGid())
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getDelFlag, 0)
                .eq(LinkDO::getEnableStatus, 0);

        // 2. 查询短链接是否存在
        LinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }

        // 4. 判断查询出的链接和请求的参数中的链接是否在同一分组（gid是否相同）
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            // 如果在同一分组，进行更新
            LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getGid, requestParam.getGid())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 0)
                    // 如果是永久，则到期时间为null，否则为给定有效期时间
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), LinkDO::getValidDate, null);
            // 3. 构造短链接对象进行更新
            LinkDO shortLinkDO = LinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(hasShortLinkDO.getFavicon())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        }else {
            // 如果不在同一分组，先删除原有链接，再插入新链接
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            // 尝试获取写锁，如果锁已被占用，则抛出异常提示用户稍后再试
            if (!rLock.tryLock()) {
                throw new ServiceException("短链接正在被访问，请稍后再试...");
            }
            try {
                // 构建更新条件，将原有链接的 delFlag 设置为 1，表示删除该链接
                LambdaUpdateWrapper<LinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                        .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkDO::getDelFlag, 0)
                        .eq(LinkDO::getEnableStatus, 0);
                LinkDO delShortLinkDO = LinkDO.builder().build();
                delShortLinkDO.setDelFlag(1);
                // 执行更新操作，删除原有链接
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);

                // 构建新的链接对象，用来保存新的链接信息，包括分组、原始链接、描述、有效日期等
                LinkDO shortLinkDO = LinkDO.builder()
                        .domain(createShortLinkDefaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDO.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDO.getShortUri())
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .favicon(getFavicon(requestParam.getOriginUrl()))
                        .build();
                // 执行插入操作，插入新链接
                baseMapper.insert(shortLinkDO);

                // 更新其他相关表，包括统计信息表、访问记录表等
                LambdaQueryWrapper<LinkStatsTodayDO> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkStatsTodayDO::getDelFlag, 0);
                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(LinkStatsTodayDO::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }

                // 更新路由表
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);

                // 更新基础数据统计表，t_link_access_stats
                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder().gid(requestParam.getGid()).build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);

                // 更新地理位置gid信息
                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder().gid(requestParam.getGid()).build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);

                // 更新操作系统位置信息
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder().gid(requestParam.getGid()).build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);

                //更新浏览器gid信息
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder().gid(requestParam.getGid()).build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                // 更新设备gid信息
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder().gid(requestParam.getGid()).build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);

                // 更新链接访问gid信息
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder().gid(requestParam.getGid()).build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);

                // 更新总的统计访问信息，t_link_access_logs
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, hasShortLinkDO.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder().gid(requestParam.getGid()).build();
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);

            } finally {
                // 在事务结束后释放写锁
                rLock.unlock();
            }
        }
        // 如果对日期进行了更改就删除缓存
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            if (hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().before(new Date())) {
                if (Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()) || requestParam.getValidDate().after(new Date())) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        // 1. 先获取整体的短链接
        String serverName = request.getServerName();
//        String fullShortUrl = serverName + "/" + shortUri;
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80)) // 过滤掉默认的80端口
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        // 2. 从缓存中获取，看原链接是否存在
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        // 2.1 如果存在，跳转，直接返回
        if (StrUtil.isNotBlank(originalLink)) {
//            shortLinkStats(fullShortUrl, null, request, response);
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        // 3. 不存在，判断布隆过滤器中是否存在
        boolean contains = bloomFilter.contains(fullShortUrl);
        // 3.1 不存在，直接跳转找不到的页
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 4. 存在，判断缓存中是否存在，避免布隆过滤器出现误判率
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        // 4.1 缓存存在，直接跳转
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 4.2 不存在，则处理数据库，获取锁，避免缓存击穿
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // 5. 获取锁之后再次查缓存是否存在
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            // 5.1 存在，仍然跳转
            if (StrUtil.isNotBlank(originalLink)) {
//                shortLinkStats(fullShortUrl, null, request, response);
                ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            // 5.2 不存在，那么查数据库
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            // 5.3 判断数据库是否存在，数据库不存在，缓存一个空值，然后跳转找不到的页面，避免缓存穿透
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 5.4 存在则查询出来，缓存到redis中
            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(LinkDO::getFullShortUrl, fullShortUrl)
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 0);
            LinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            // 5.5 查询出来判断是否过期，如果是，则跳转找不到页面
            if (shortLinkDO == null || (shortLinkDO.getValidDate()!=null && shortLinkDO.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 6. 设置缓存
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
//            shortLinkStats(fullShortUrl,  shortLinkDO.getGid(), request, response);
            ShortLinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), statsRecord);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 构建短链接的访问统计记录并设置用户信息。
     *
     * @param fullShortUrl 完整的短链接
     * @param request      请求对象
     * @param response     响应对象
     * @return 构建的短链接访问统计记录DTO
     */
    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        // 标志是否为UV的第一次访问
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        // 获取请求中的Cookie数组
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        // UV标识
        AtomicReference<String> uv = new AtomicReference<>();

        // 添加响应Cookie的任务
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv.get());
        };

        // 如果Cookie数组不为空，则尝试获取名为"uv"的Cookie值
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        // 如果存在"uv"的Cookie，则将UV标识设置为Cookie的值
                        uv.set(each);
                        // 尝试将UV标识添加到Redis的集合中，判断是否为UV的第一次访问
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            // 如果Cookie数组为空，则执行添加响应Cookie的任务
            addResponseCookieTask.run();
        }

        // 获取请求的真实IP、操作系统、浏览器、设备和网络信息
        String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));

        // 将用户IP添加到Redis的集合中，判断是否为UIP的第一次访问
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;

        // 构建并返回短链接访问统计记录DTO
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    @Override
/**
 * 统计短链接访问量及相关信息的方法。
 *
 * @param fullShortUrl 短链接的完整URL
 * @param gid 分组ID
 * @param statsRecord 短链接访问记录DTO对象
 */
    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("fullShortUrl", fullShortUrl);
        producerMap.put("gid", gid);
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        shortLinkStatsSaveProducer.send(producerMap);
//        // 如果未提供完整短链接URL，则使用statsRecord中的URL
//        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
//
//        // 获取短链接URL对应的读写锁
//        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
//        RLock rLock = readWriteLock.readLock();
//
//        try {
//            // 尝试获取锁，若无法获取则将访问记录推送至消息队列延后处理
//            if (!rLock.tryLock()) {
//                delayShortLinkStatsProducer.send(statsRecord);
//                return;
//            }
//
//            // 如果未提供分组ID，则从数据库查询关联的分组ID
//            if (StrUtil.isBlank(gid)) {
//                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
//                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
//                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
//                gid = shortLinkGotoDO.getGid();
//            }
//
//            // 获取当前时间的小时和星期
//            int hour = DateUtil.hour(new Date(), true);
//            Week week = DateUtil.dayOfWeekEnum(new Date());
//            int weekValue = week.getIso8601Value();
//
//            // 构建短链接访问统计对象
//            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
//                    .pv(1)
//                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
//                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
//                    .hour(hour)
//                    .weekday(weekValue)
//                    .fullShortUrl(fullShortUrl)
//                    .gid(gid)
//                    .date(new Date())
//                    .build();
//
//            // 插入短链接访问统计数据
//            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
//
//            // 获取请求IP的地理位置信息
//            Map<String, Object> localeParamMap = new HashMap<>();
//            localeParamMap.put("key", statsLocaleAmapKey);
//            localeParamMap.put("ip", statsRecord.getRemoteAddr());
//            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
//            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
//            String infoCode = localeResultObj.getString("infocode");
//            String actualProvince = "未知";
//            String actualCity = "未知";
//
//            // 解析地理位置信息并插入短链接地域统计数据
//            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
//                String province = localeResultObj.getString("province");
//                boolean unknownFlag = StrUtil.equals(province, "[]");
//                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
//                        .province(actualProvince = unknownFlag ? actualProvince : province)
//                        .city(actualCity = unknownFlag ? actualCity : localeResultObj.getString("city"))
//                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
//                        .cnt(1)
//                        .fullShortUrl(fullShortUrl)
//                        .country("中国")
//                        .gid(gid)
//                        .date(new Date())
//                        .build();
//                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
//            }
//
//            // 插入短链接操作系统统计数据
//            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
//                    .os(statsRecord.getOs())
//                    .cnt(1)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .date(new Date())
//                    .build();
//            linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
//
//            // 插入短链接浏览器统计数据
//            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
//                    .browser(statsRecord.getBrowser())
//                    .cnt(1)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .date(new Date())
//                    .build();
//            linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
//
//            // 插入短链接设备统计数据
//            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
//                    .device(statsRecord.getDevice())
//                    .cnt(1)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .date(new Date())
//                    .build();
//            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
//
//            // 插入短链接网络统计数据
//            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
//                    .network(statsRecord.getNetwork())
//                    .cnt(1)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .date(new Date())
//                    .build();
//            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
//
//            // 插入短链接访问日志
//            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
//                    .user(statsRecord.getUv())
//                    .ip(statsRecord.getRemoteAddr())
//                    .browser(statsRecord.getBrowser())
//                    .os(statsRecord.getOs())
//                    .network(statsRecord.getNetwork())
//                    .device(statsRecord.getDevice())
//                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .build();
//            linkAccessLogsMapper.insert(linkAccessLogsDO);
//
//            // 更新短链接访问统计总量
//            baseMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
//
//            // 插入短链接今日访问统计数据
//            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
//                    .todayPv(1)
//                    .todayUv(statsRecord.getUvFirstFlag() ? 1 : 0)
//                    .todayUip(statsRecord.getUipFirstFlag() ? 1 : 0)
//                    .gid(gid)
//                    .fullShortUrl(fullShortUrl)
//                    .date(new Date())
//                    .build();
//            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
//        } catch (Throwable ex) {
//            log.error("短链接访问量统计异常", ex);
//        } finally {
//            rLock.unlock();
//        }
    }
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }


    /**
     * 批量创建短链接并返回结果
     *
     * @param requestParam 包含批量创建所需参数的请求DTO
     * @return 包含批量创建结果的响应DTO
     */
    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        // 从请求DTO中获取原始URL列表和描述信息列表
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();

        // 用于存储批量创建结果的列表
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();

        // 遍历原始URL列表，逐个创建短链接
        for (int i = 0; i < originUrls.size(); i++) {
            // 创建用于单个短链接创建的请求DTO，并设置对应的原始URL和描述信息
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));

            try {
                // 调用单个短链接创建方法
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);

                // 构建单个短链接的基本信息响应DTO
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();

                // 将单个短链接的基本信息添加到结果列表中
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                // 如果创建短链接过程中发生异常，记录错误日志，并继续处理下一个原始URL
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }

        // 构建批量创建短链接的响应DTO，包含总数和各个短链接的基本信息列表
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }
}




