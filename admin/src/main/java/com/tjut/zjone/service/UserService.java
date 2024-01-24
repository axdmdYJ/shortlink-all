package com.tjut.zjone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.dao.entity.UserDO;
import com.tjut.zjone.dto.resp.UserLoginRespDTO;
import com.tjut.zjone.dto.resp.UserRespDTO;
import com.tjut.zjone.dto.resq.UserLoginReqDTO;
import com.tjut.zjone.dto.resq.UserRegisterReqDTO;
import com.tjut.zjone.dto.resq.UserUpdateReqDTO;
import org.springframework.stereotype.Service;

/**
* @author a0000
* @description 针对表【t_user】的数据库操作Service
* @createDate 2024-01-06 00:31:25
*/
@Service
public interface UserService extends IService<UserDO> {


    UserRespDTO getUserByUsername(String username);

    /**
     * 查看用户是否存在
     * @param username 用户名
     * @return 是否存在
     */
    Boolean hasUsername(String username);

    void userRegister(UserRegisterReqDTO registerParam);

    /**
     * 用户修改
     * @param requestParam 用户信息
     */
    void userUpdate(UserUpdateReqDTO requestParam);

    /**
     * 用户登陆
     * @param requestParam 用户名和密码
     * @return token
     */
    Result<UserLoginRespDTO> userLogin(UserLoginReqDTO requestParam);

    Boolean userHasLogin(String username, String token);

    void userLogout(String username, String token);
}
