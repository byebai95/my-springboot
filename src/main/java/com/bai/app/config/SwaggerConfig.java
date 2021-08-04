package com.bai.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket getDocket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .build();
    }

    private ApiInfo getApiInfo(){
        return new ApiInfo(
                "my-springboot",
                "个人 springboot 测试",
                "1.0.0",
                "www.baidu.com",
                new Contact("bye","www.bye.com","bye@163.com"),
                "Apache2.0",
                "www.apache.com",
                Arrays.asList(
                        new StringVendorExtension("color", "red"),
                        new StringVendorExtension("size", "100px")
                ));

    }

}
