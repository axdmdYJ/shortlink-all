package com.tjut.zjone.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjut.zjone.common.constant.RedisCacheConstant;
import com.tjut.zjone.common.convention.exception.ClientException;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.common.enums.UserErrorCodeEnum;
import com.tjut.zjone.dao.entity.UserDO;
import com.tjut.zjone.dao.mapper.UserMapper;
import com.tjut.zjone.dto.req.GroupSaveNameReqDTO;
import com.tjut.zjone.dto.resp.UserLoginRespDTO;
import com.tjut.zjone.dto.resp.UserRespDTO;
import com.tjut.zjone.dto.req.UserLoginReqDTO;
import com.tjut.zjone.dto.req.UserRegisterReqDTO;
import com.tjut.zjone.dto.req.UserUpdateReqDTO;
import com.tjut.zjone.remote.ShortLinkRemoteService;
import com.tjut.zjone.service.GroupService;
import com.tjut.zjone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
* @author a0000
* @description 针对表【t_user】的数据库操作Service实现
* @createDate 2024-01-06 00:31:25
*/
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO>
    implements UserService {
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final UserMapper userMapper;
    private final GroupService groupService;
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
        return userRegisterCachePenetrationBloomFilter.contains(username);
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
                userRegisterCachePenetrationBloomFilter.add(registerParam.getUsername());
                groupService.saveGroupName(registerParam.getUsername(),"默认分组");
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

    @Override
    public Result<UserLoginRespDTO> userLogin(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO user = userMapper.selectOne(queryWrapper);
        if(user == null){
            throw new ClientException("用户不存在");
        }
//        Boolean hasedLogin = stringRedisTemplate.hasKey("login_"+requestParam.getUsername());
//        if (hasedLogin !=null && hasedLogin){
//            throw new ClientException("用户已登陆");
//        }
        String token = UUID.randomUUID().toString();
        //避免重复登陆
        stringRedisTemplate.opsForHash().put("login_"+requestParam.getUsername(), token, JSON.toJSONString(user));
        return Results.success(new UserLoginRespDTO(token));
    }

    @Override
    public Boolean userHasLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get("login_"+ username, token) != null;
    }

    @Override
    public void userLogout(String username, String token) {
        stringRedisTemplate.opsForHash().delete("login_"+ username, token);
    }

}




