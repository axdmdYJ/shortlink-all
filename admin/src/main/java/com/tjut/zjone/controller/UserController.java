package com.tjut.zjone.controller;


import cn.hutool.core.bean.BeanUtil;
import com.tjut.zjone.common.convention.exception.ClientException;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.common.enums.UserErrorCodeEnum;
import com.tjut.zjone.dto.resp.UserActualRespDTO;
import com.tjut.zjone.dto.resp.UserRespDTO;
import com.tjut.zjone.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Resource
    private UserService userService;
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username){
       return Results.success(userService.getUserByUsername(username));
    }
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable String username){
       return  Results.success(BeanUtil.toBean(userService.getUserByUsername(username),UserActualRespDTO.class));
    }

    @GetMapping("/api/short-link/admin/v1/actual/user/has-username")
    public Result<Boolean> hasUsername(String username){
            return Results.success(userService.hasUsername(username));
    }


}
