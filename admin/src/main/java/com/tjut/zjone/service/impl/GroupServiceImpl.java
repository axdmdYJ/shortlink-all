package com.tjut.zjone.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.dao.entity.GroupDO;
import com.tjut.zjone.service.GroupService;
import com.tjut.zjone.dao.mapper.GroupMapper;
import org.springframework.stereotype.Service;

/**
* @author a0000
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2024-01-24 19:12:58
*/
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO>
    implements GroupService {


    @Override
    public void save(String group_name) {

    }
}




