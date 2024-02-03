package com.tjut.zjone.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tjut.zjone.common.biz.user.UserContext;
import com.tjut.zjone.common.convention.exception.ServiceException;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.dao.entity.GroupDO;
import com.tjut.zjone.dao.mapper.GroupMapper;
import com.tjut.zjone.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.tjut.zjone.remote.RecycleBinService;
import com.tjut.zjone.remote.ShortLinkRemoteService;
import com.tjut.zjone.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * URL 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;



    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    private final ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }
}