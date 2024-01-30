package com.tjut.zjone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.zjone.dao.entity.GroupDO;
import com.tjut.zjone.dto.req.GroupSaveNameReqDTO;
import com.tjut.zjone.dto.req.ShortLinkGroupSortDTO;
import com.tjut.zjone.dto.req.ShortLinkGroupUpdateReqDTO;
import com.tjut.zjone.dto.resp.ShortLinkGroupListRespDTO;

import java.util.List;

/**
* @author a0000
* @description 针对表【t_group】的数据库操作Service
* @createDate 2024-01-24 19:12:58
*/
public interface GroupService extends IService<GroupDO> {


    /**
     * 新增短链接组名
     * @param requestParam 包含短链接组名
     */
    void saveGroupName(GroupSaveNameReqDTO requestParam);

    void saveGroupName(String username, String groupName);

    List<ShortLinkGroupListRespDTO> getGroupList();


    /**
     * 修改短链接分组信息
     * @param requestParam 修改短链接分组信息传参
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     * @param gid 分组标识
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam 短链接分组排序传参
     */
    void sortGroup(List<ShortLinkGroupSortDTO> requestParam);

}
