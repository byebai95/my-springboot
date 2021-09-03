package com.bai.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = {"com.bai.app.dao"})
@SpringBootApplication
public class MySpringbootApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MySpringbootApplication.class);
        //设置 banner 输出模式 ，默认为 console
        application.setBannerMode(Banner.Mode.OFF);

        application.setWebApplicationType(WebApplicationType.SERVLET);

        //日志是否打印 app 启动耗时, 默认开启
        application.setLogStartupInfo(true);

        //System.setProperty("spring.boot.enableautoconfiguration","false");

        application.run(args);

    }

}
