package com.zozo.zozopicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.zozo.zozopicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)

public class ZozoPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZozoPictureBackendApplication.class, args);

    }

}
