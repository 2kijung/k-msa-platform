package com.kimdevops.portfolio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 업로드된 파일을 정적 리소스로 서빙.
 * ./uploads/ 폴더의 파일을 /api/uploads/** 경로로 접근 가능하게 함.
 * (context-path가 /api 이므로 실제 URL은 /api/uploads/파일명)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}
