package com.tjut.zjone.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.dao.entity.LinkDO;
import com.tjut.zjone.dao.mapper.LinkMapper;
import com.tjut.zjone.dto.req.ShortLinkCreateReqDTO;
import com.tjut.zjone.dto.resp.ShortLinkCreateRespDTO;
import com.tjut.zjone.service.LinkService;
import org.springframework.stereotype.Service;

import static com.tjut.zjone.util.HashUtil.hashToBase62;

/**
* @author a0000
* @description 针对表【t_link】的数据库操作Service实现
* @createDate 2024-01-26 19:17:33
*/
@Service
public class LinkServiceImpl extends ServiceImpl<LinkMapper, LinkDO>
    implements LinkService {

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        String suffix = generateSuffix(requestParam);
        String fullShortUrl = requestParam.getDomain() + "/" + suffix;
        LinkDO linkDO = BeanUtil.toBean(requestParam, LinkDO.class);
        linkDO.setEnableStatus(0);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setShortUri(suffix);
        baseMapper.insert(linkDO);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .build();
    }

    public String generateSuffix(ShortLinkCreateReqDTO requestParam){
        String originUrl = requestParam.getOriginUrl();
        return hashToBase62(originUrl);
    }
}




