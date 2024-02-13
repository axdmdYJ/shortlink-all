package com.tjut.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.shortlink.admin.common.biz.user.UserContext;
import com.tjut.shortlink.admin.common.convention.exception.ClientException;
import com.tjut.shortlink.admin.common.convention.result.Result;
import com.tjut.shortlink.admin.dao.entity.GroupDO;
import com.tjut.shortlink.admin.dao.mapper.GroupMapper;
import com.tjut.shortlink.admin.dto.req.GroupSaveNameReqDTO;
import com.tjut.shortlink.admin.dto.req.ShortLinkGroupSortDTO;
import com.tjut.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.tjut.shortlink.admin.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.tjut.shortlink.admin.dto.resp.ShortLinkGroupListRespDTO;
import com.tjut.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.tjut.shortlink.admin.service.GroupService;
import com.tjut.shortlink.admin.util.GidGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static com.tjut.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;
import static com.tjut.shortlink.admin.util.GidGeneratorUtil.generateRandomString;

/**
* @author lyj
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2024-01-24 19:12:58
*/
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO>
    implements GroupService {

    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Override
    public void saveGroupName(GroupSaveNameReqDTO requestParam) {
         String gid = generateRandomString();
         while (gidIfAbsent(UserContext.getUsername(), gid)){
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
    public void saveGroupName(String username, String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0);
            List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(groupDOList) && groupDOList.size() == groupMaxNum) {
                throw new ClientException(String.format("已超出最大分组数：%d", groupMaxNum));
            }
            String gid;
            do {
                gid = GidGeneratorUtil.generateRandomString();
            } while (!gidIfAbsent(username, gid));
            GroupDO groupDO = GroupDO.builder()
                    .gid(gid)
                    .sortOrder(0)
                    .username(username)
                    .name(groupName)
                    .build();
            baseMapper.insert(groupDO);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取用户的短链接分组列表，包括每个分组下的短链接数量
     *
     * @return 包含分组信息及短链接数量的响应DTO列表
     */
    @Override
    public List<ShortLinkGroupListRespDTO> getGroupList() {
        // 1. 构建查询条件，查询用户拥有的未删除的分组，并按照排序字段降序排列
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder);

        // 2. 查询数据库获取用户的分组列表
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);

        // 3. 查询每个分组下的短链接数量
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkActualRemoteService
                .listGroupShortLinkCount(groupDOList.stream().map(GroupDO::getGid).toList());

        // 4. 将 GroupDO 转换为 ShortLinkGroupListRespDTO
        List<ShortLinkGroupListRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupListRespDTO.class);

        // 5. 遍历短链接数量结果，并将数量设置到对应的分组DTO中
        // each是数据库查出来的，然后将业务逻辑查询的用户分组列表进行过滤，由于 filter 操作后得到的是一个流，通过 findFirst 取得流中的第一个元素。
        shortLinkGroupRespDTOList.forEach(each -> {
            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
                    .findFirst();
            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
        });

        // 6. 返回包含分组信息及短链接数量的响应DTO列表
        return shortLinkGroupRespDTOList;
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



    public boolean gidIfAbsent(String username,String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO one = baseMapper.selectOne(queryWrapper);
        return one != null;
    }
}




