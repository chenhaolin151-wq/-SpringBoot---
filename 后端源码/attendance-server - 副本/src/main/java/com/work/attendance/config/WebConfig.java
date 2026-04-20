package com.work.attendance.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    // 🌟 动态注入配置文件中的路径
    @Value("${file-upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 动态注入路径，确保无论部署在 D 盘还是 Linux 都能找到
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login", "/api/user/register")
                .excludePathPatterns("/api/attendance/exportReport") // 🌟 关键：排除掉报表导出接口
                .excludePathPatterns("/api/file/export");           // 确保之前的导出也能用
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*") // 🌟 必须允许所有请求头
                .allowCredentials(true);
    }
}