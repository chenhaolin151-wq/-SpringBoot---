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
        // 确保路径以 / 结尾，并且拼接 file: 前缀
        String locations = "file:" + (uploadPath.endsWith("/") ? uploadPath : uploadPath + "/");

        registry.addResourceHandler("/files/**")
                .addResourceLocations(locations);
    }
}