package com.tjut.zjone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.dao.entity.GroupDO;
import com.tjut.zjone.dao.mapper.GroupMapper;
import com.tjut.zjone.dto.resq.GroupSaveNameReqDTO;
import com.tjut.zjone.service.GroupService;
import org.springframework.stereotype.Service;

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
                .name(requestParam.getName())
                .gid(gid)
                .build();
        baseMapper.insert(groupDO);
    }
    public boolean gidIfAbsent(String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername,null);
        GroupDO one = baseMapper.selectOne(queryWrapper);
        return one != null;
    }
}




