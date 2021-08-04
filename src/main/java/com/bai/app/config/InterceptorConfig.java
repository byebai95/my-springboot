package com.bai.app.config;

import com.bai.app.config.repo.ActionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 14:36
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ActionInterceptor()).addPathPatterns("/**");
    }
}
