package com.tjut.zjone.controller;


import com.tjut.zjone.dto.resp.UserRespDTO;
import com.tjut.zjone.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Resource
    private UserService userService;
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public UserRespDTO getUserByUsername(@PathVariable String username){
    // 调用UserService的方法获取用户信息
        return userService.getUserByUsername(username);
    }




}
