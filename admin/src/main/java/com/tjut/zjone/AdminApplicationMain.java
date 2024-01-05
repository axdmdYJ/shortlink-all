package com.tjut.zjone;


import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tjut.zjone.dao.mapper")// 扫描Mapper接口
public class AdminApplicationMain {

    public static void main(String[] args) {
        SpringApplication
                .run(AdminApplicationMain.class, args);
    }
}