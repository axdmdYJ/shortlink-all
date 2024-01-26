package com.tjut.zjone;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tjut.zjone.dao.mapper")// 扫描Mapper接口
public class ShortLinkApplication {
    public static void main(String[] args) {
        SpringApplication
                .run(ShortLinkApplication.class, args);
    }
}