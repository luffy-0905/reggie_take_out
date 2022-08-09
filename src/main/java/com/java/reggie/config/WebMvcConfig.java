package com.java.reggie.config;

import com.java.reggie.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    /**
     *  设置静态资源映射（没有将静态资源放在static下的解决方案）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
        log.info("开始静态资源映射");
    }

    /**
     * 设置拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始拦截请求");
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")//拦截所有请求，包括静态资源
                .excludePathPatterns("/employee/login","/employee/logout","/backend/**","/front/**");//放行的请求
    }
}
