package com.tjut.zjone.controller;


import cn.hutool.core.bean.BeanUtil;
import com.tjut.zjone.common.convention.result.Result;
import com.tjut.zjone.common.convention.result.Results;
import com.tjut.zjone.dto.resp.UserActualRespDTO;
import com.tjut.zjone.dto.resp.UserLoginRespDTO;
import com.tjut.zjone.dto.resp.UserRespDTO;
import com.tjut.zjone.dto.req.UserLoginReqDTO;
import com.tjut.zjone.dto.req.UserRegisterReqDTO;
import com.tjut.zjone.dto.req.UserUpdateReqDTO;
import com.tjut.zjone.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 注册用户
     * @param registerParam
     * @return
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> userRegister(@RequestBody UserRegisterReqDTO registerParam){
        userService.userRegister(registerParam);
        return Results.success();
    }

    @PutMapping("/api/short-link/admin/v1/user")
    public Result<Void> userUpdate(@RequestBody UserUpdateReqDTO requestParam){
        userService.userUpdate(requestParam);
        return Results.success();
    }

    @PostMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> userLogin(@RequestBody UserLoginReqDTO requestParam){
        return userService.userLogin(requestParam);
    }

    @GetMapping("/api/short-link/admin/v1/user/check-login")
    public Result<Boolean> userHasLogin(@RequestParam("username") String username ,@RequestParam("token") String token){
        Boolean hasLogin = userService.userHasLogin(username, token);
        return  Results.success(hasLogin);
    }

    @DeleteMapping("/api/short-link/admin/v1/user/logout")
    public  Result<Void> userLogout(@RequestParam("username") String username, @RequestParam("token") String token){
        userService.userLogout(username, token);
        return Results.success();
    }



}
