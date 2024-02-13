package com.tjut.shortlink.admin;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.tjut.shortlink.admin.dao.mapper")// 扫描Mapper接口
@EnableFeignClients("com.tjut.shortlink.admin.remote")
public class AdminApplicationMain {

    public static void main(String[] args) {
        SpringApplication
                .run(AdminApplicationMain.class, args);
    }
}