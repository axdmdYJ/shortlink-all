package com.tjut.zjone.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public String getUserByUsername(@PathVariable String username){
        // 处理逻辑// 处理逻辑
        return "success" + username;
    }




}
