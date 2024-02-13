package com.tjut.shortlink.project;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@MapperScan("com.tjut.shortlink.project.dao.mapper")// 扫描Mapper接口
@EnableDiscoveryClient
public class ShortLinkApplication {
    public static void main(String[] args) {
        SpringApplication
                .run(ShortLinkApplication.class, args);
    }
}