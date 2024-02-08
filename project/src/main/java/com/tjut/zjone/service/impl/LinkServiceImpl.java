package com.tjut.zjone.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.common.convention.exception.ClientException;
import com.tjut.zjone.common.convention.exception.ServiceException;
import com.tjut.zjone.common.enums.VailDateTypeEnum;
import com.tjut.zjone.dao.entity.*;
import com.tjut.zjone.dao.mapper.*;
import com.tjut.zjone.dto.req.ShortLinkBatchCreateReqDTO;
import com.tjut.zjone.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.dto.req.ShortLinkPageReqDTO;
import com.tjut.zjone.dto.req.ShortLinkUpdateReqDTO;
import com.tjut.zjone.dto.resp.*;
import com.tjut.zjone.service.LinkService;
import com.tjut.zjone.util.LinkUtil;
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
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.tjut.zjone.common.constant.RedisKeyConstant.*;
import static com.tjut.zjone.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static com.tjut.zjone.util.HashUtil.hashToBase62;

/**
* @author a0000
* @description 针对表【t_link】的数据库操作Service实现
* @createDate 2024-01-26 19:17:33
*/
@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * 开发环境默认域名测试
     */
    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;



    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
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
            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, fullShortUrl);
            LinkDO shortLink = baseMapper.selectOne(queryWrapper);
            if (shortLink != null){
                log.warn("短链接: {} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
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
            originUrl += System.currentTimeMillis();
            shortUri = hashToBase62(originUrl);

            if (!bloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) break;
            generate_count++;
        }
        return shortUri;
    }
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<LinkDO> queryWrapper = Wrappers.query(new LinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
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
        // 1. 根据分组标识（gid）、完整短链接（fullShortUrl）、删除标识（delFlag）、启用状态（enableStatus）筛选数据库满足条件的数据
        LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                .eq(LinkDO::getGid, requestParam.getGid())
                .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(LinkDO::getDelFlag, 0)
                .eq(LinkDO::getEnableStatus, 0);

        // 2. 查询短链接是否存在
        LinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }

        // 3. 构造短链接对象进行更新
        LinkDO shortLinkDO = LinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .shortUri(hasShortLinkDO.getShortUri())
                .clickNum(hasShortLinkDO.getClickNum())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();

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
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            // 如果不在同一分组，先删除原有链接，再插入新链接
            LambdaUpdateWrapper<LinkDO> updateWrapper = Wrappers.lambdaUpdate(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(LinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(LinkDO::getDelFlag, 0)
                    .eq(LinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
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
            shortLinkStats(fullShortUrl, null, request, response);
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
                shortLinkStats(fullShortUrl, null, request, response);
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
            shortLinkStats(fullShortUrl,  shortLinkDO.getGid(), request, response);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        //保证修改的原子性
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
//            在代码中，uv 用于存储从 Cookie 中读取到的用户标识。由于这个操作涉及到多线程，为了确保线程安全，使用了 AtomicReference 来保证 uv 引用的原子性。
            AtomicReference<String> uv = new AtomicReference<>();
            Runnable addResponseCookieTask = () -> {
                // 设置cookie值，保证不一样
                uv.set(UUID.fastUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get());
                // 有效时间
                uvCookie.setMaxAge(60 * 60 * 24 * 30);
                // 找到fullShortUrl 第一个“/” 的位置，截取fullShortUrl.length个长度
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                ((HttpServletResponse) response).addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
            };
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            uv.set(each);
                            // 如果 Cookie 存在，将其值添加到 Redis 集合中，并标记不是用户的首次访问
                            // 如果 added 为 null，说明添加操作失败或出现异常。
                            // 如果 added 为 0，说明该 UV 已经存在于集合中，不是用户的首次访问。
                            // 如果 added 大于 0，说明成功将新的 UV 添加到集合中，标记用户是首次访问。
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask);
            } else {
                addResponseCookieTask.run();
            }
            // 获取用户地址ip
            String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
            // 看是否是第一次点击
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String, Object> localeParamMap = new HashMap<>();
            // 获取高德api请求参数
            localeParamMap.put("key", statsLocaleAmapKey);
            localeParamMap.put("ip", remoteAddr);
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            // 将高德api返回信息转换成对象，然后获取返回信息
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infoCode = localeResultObj.getString("infocode");
            String actualProvince;
            String actualCity;
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localeResultObj.getString("province");
                // 判断是不是未知，如果不是，就将信息存到数据库中
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(new Date())
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
                // 监控操作系统访问
                String os = LinkUtil.getOs(((HttpServletRequest) request));
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .os(os)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
                // 监控浏览器访问
                String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .browser(browser)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);

                // 统计高频访问ip
//                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
//                        .user(uv.get())
//                        .ip(remoteAddr)
//                        .browser(browser)
//                        .os(os)
//                        .gid(gid)
//                        .fullShortUrl(fullShortUrl)
//                        .build();
//                linkAccessLogsMapper.insert(linkAccessLogsDO);

                // 监控访问设备
                String device = LinkUtil.getDevice(((HttpServletRequest) request));
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .device(device)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);

                // 监控访问网络
                String network = LinkUtil.getNetwork(((HttpServletRequest) request));
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .network(network)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

                // 监控信息
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .user(uv.get())
                        .ip(remoteAddr)
                        .browser(browser)
                        .os(os)
                        .network(network)
                        .device(device)
                        .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);
                baseMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipFirstFlag ? 1 : 0);
                LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                        .todayPv(1)
                        .todayUv(uvFirstFlag.get() ? 1 : 0)
                        .todayUip(uipFirstFlag ? 1 : 0)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
            }
        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        }
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


}




