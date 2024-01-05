package com.tjut.zjone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.dao.domain.UserDO;
import com.tjut.zjone.dao.mapper.UserMapper;
import com.tjut.zjone.dto.resp.UserRespDTO;
import com.tjut.zjone.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author a0000
* @description 针对表【t_user】的数据库操作Service实现
* @createDate 2024-01-06 00:31:25
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO>
    implements UserService {
    @Override
    public UserRespDTO getUserByUsername(String username) {
            // 调用Mapper方法查询用户
            // 假设查询结果不为空
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getUsername, username);
        UserDO userDO = this.getOne(queryWrapper);
            if (userDO == null) {
                return null;
            }
            // 转换为响应DTO
            UserRespDTO userRespDTO = new UserRespDTO();
            BeanUtils.copyProperties(userDO, userRespDTO);
            return userRespDTO;
    }
}




