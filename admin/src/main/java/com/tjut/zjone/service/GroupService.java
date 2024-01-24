package com.tjut.zjone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.zjone.dao.entity.GroupDO;
import com.tjut.zjone.dto.resq.GroupSaveNameReqDTO;

/**
* @author a0000
* @description 针对表【t_group】的数据库操作Service
* @createDate 2024-01-24 19:12:58
*/
public interface GroupService extends IService<GroupDO> {


    void saveGroupName(GroupSaveNameReqDTO requestParam);
}
