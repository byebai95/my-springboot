package com.bai.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import java.io.IOException;

/**
 * @Author:baizhuang
 * @Date:2021/8/4 14:42
 */
@Slf4j
@Configuration
public class FilterConfig implements Filter {

    @Override
    public void init(javax.servlet.FilterConfig filterConfig) throws ServletException {
        log.info("filter:init");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("filter:doFilter");
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {
        log.info("filter:destroy");
    }
}
