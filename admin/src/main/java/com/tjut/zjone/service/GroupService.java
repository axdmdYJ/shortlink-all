package com.tjut.zjone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.zjone.dao.entity.GroupDO;
import com.tjut.zjone.dto.req.GroupSaveNameReqDTO;
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

    List<ShortLinkGroupListRespDTO> getGroupList();


    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);
}
