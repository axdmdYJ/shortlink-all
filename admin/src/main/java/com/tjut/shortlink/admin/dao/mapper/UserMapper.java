package com.tjut.shortlink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.shortlink.admin.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【t_user】的数据库操作Mapper
*/
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}




