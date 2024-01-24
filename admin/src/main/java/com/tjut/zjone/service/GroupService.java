package com.tjut.zjone.service;

import com.tjut.zjone.dao.entity.GroupDO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author a0000
* @description 针对表【t_group】的数据库操作Service
* @createDate 2024-01-24 19:12:58
*/
public interface GroupService extends IService<GroupDO> {


    void save(String group_name);
}
