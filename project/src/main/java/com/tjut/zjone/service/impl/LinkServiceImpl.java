package com.tjut.zjone.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.common.convention.exception.ServiceException;
import com.tjut.zjone.dao.entity.LinkDO;
import com.tjut.zjone.dao.mapper.LinkMapper;
import com.tjut.zjone.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.zjone.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

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
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String suffix = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + suffix;
        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setEnableStatus(0);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setShortUri(suffix);
        try {
            baseMapper.insert(linkDO);
        } catch (DuplicateKeyException e) {
            LambdaQueryWrapper<LinkDO> queryWrapper = Wrappers.lambdaQuery(LinkDO.class)
                    .eq(LinkDO::getFullShortUrl, fullShortUrl);
            LinkDO shortLink = baseMapper.selectOne(queryWrapper);
            if (shortLink != null){
                log.warn("短链接: {} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        bloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .build();
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
}




