package com.work.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 意思是：如果访问路径开头是 /files/，就去 D:/uploads/ 找东西
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:D:/uploads/");
    }
}