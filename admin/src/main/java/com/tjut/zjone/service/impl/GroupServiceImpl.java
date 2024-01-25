package com.tjut.zjone.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.common.biz.user.UserContext;
import com.tjut.zjone.dao.entity.GroupDO;
import com.tjut.zjone.dao.mapper.GroupMapper;
import com.tjut.zjone.dto.req.GroupSaveNameReqDTO;
import com.tjut.zjone.dto.req.ShortLinkGroupSortDTO;
import com.tjut.zjone.dto.req.ShortLinkGroupUpdateReqDTO;
import com.tjut.zjone.dto.resp.ShortLinkGroupListRespDTO;
import com.tjut.zjone.service.GroupService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tjut.zjone.util.GidGeneratorUtil.generateRandomString;

/**
* @author lyj
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2024-01-24 19:12:58
*/
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO>
    implements GroupService {


    @Override
    public void saveGroupName(GroupSaveNameReqDTO requestParam) {
         String gid = generateRandomString();
         while (gidIfAbsent(gid)){
             gid = generateRandomString();
         }
        GroupDO groupDO = GroupDO.builder()
                .sortOrder(0)
                .username(UserContext.getUsername())
                .name(requestParam.getName())
                .gid(gid)
                .build();
        baseMapper.insert(groupDO);
    }

    @Override
    public List<ShortLinkGroupListRespDTO> getGroupList() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder);
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
        return BeanUtil.copyToList(groupDOList, ShortLinkGroupListRespDTO.class);
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        baseMapper.update(groupDO, queryWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO, queryWrapper);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortDTO> requestParam) {
        requestParam.forEach(each->{
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO,queryWrapper);
        });


    }

    public boolean gidIfAbsent(String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername,UserContext.getUsername());
        GroupDO one = baseMapper.selectOne(queryWrapper);
        return one != null;
    }
}




