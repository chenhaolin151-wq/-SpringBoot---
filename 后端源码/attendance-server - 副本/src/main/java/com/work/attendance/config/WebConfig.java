package com.work.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 🌟 动态注入配置文件中的路径
    @Value("${file-upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 动态注入路径，确保无论部署在 D 盘还是 Linux 都能找到
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath);
    }
}