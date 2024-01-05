package com.tjut.zjone.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tjut.zjone.dao.domain.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
* @description 针对表【t_user】的数据库操作Mapper
*/
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}




