package com.tjut.zjone.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjut.zjone.dao.domain.UserDO;
import com.tjut.zjone.dto.resp.UserRespDTO;
import org.springframework.stereotype.Service;

/**
* @author a0000
* @description 针对表【t_user】的数据库操作Service
* @createDate 2024-01-06 00:31:25
*/
@Service
public interface UserService extends IService<UserDO> {


    UserRespDTO getUserByUsername(String username);

}
