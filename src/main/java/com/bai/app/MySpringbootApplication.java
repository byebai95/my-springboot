package com.bai.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = {"com.bai.app.dao"})
@SpringBootApplication
public class MySpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringbootApplication.class, args);
    }

}
