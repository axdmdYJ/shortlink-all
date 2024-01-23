package com.tjut.zjone.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.common.constant.RedisCacheConstant;
import com.tjut.zjone.common.convention.exception.ClientException;
import com.tjut.zjone.common.enums.UserErrorCodeEnum;
import com.tjut.zjone.dao.domain.UserDO;
import com.tjut.zjone.dao.mapper.UserMapper;
import com.tjut.zjone.dto.resp.UserRespDTO;
import com.tjut.zjone.dto.resq.UserRegisterReqDTO;
import com.tjut.zjone.dto.resq.UserUpdateReqDTO;
import com.tjut.zjone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author a0000
* @description 针对表【t_user】的数据库操作Service实现
* @createDate 2024-01-06 00:31:25
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO>
    implements UserService {
    private final RBloomFilter<String> userRegisterCzachePenetrationBloomFilter;

    private final RedissonClient redissonClient;
    @Autowired
    UserMapper userMapper;
    @Override
    public UserRespDTO getUserByUsername(String username) {
            // 调用Mapper方法查询用户
            // 假设查询结果不为空
        LambdaQueryWrapper<UserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDO::getUsername, username);
        UserDO userDO = this.getOne(queryWrapper);
            if (userDO == null) {
                throw new ClientException(UserErrorCodeEnum.USER_NULL);
            }
            // 转换为响应DTO
            UserRespDTO userRespDTO = new UserRespDTO();
            BeanUtils.copyProperties(userDO, userRespDTO);
            return userRespDTO;
    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCzachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void userRegister(UserRegisterReqDTO registerParam) {
        if (hasUsername(registerParam.getUsername())){
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXISTS);
        }
        RLock rLock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY);
        try{
            if (rLock.tryLock()){
                int inserted=userMapper.insert(BeanUtil.toBean(registerParam,UserDO.class));
                if(inserted < 1){
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_FAILE);
                }
                userRegisterCzachePenetrationBloomFilter.add(registerParam.getUsername());
            }
        }finally {
            rLock.unlock();
        }

    }

    @Override
    public void userUpdate(UserUpdateReqDTO requestParam) {
        //todo 验证当前用户是否为登陆用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        userMapper.update(BeanUtil.toBean(requestParam,UserDO.class),updateWrapper);
    }

}




