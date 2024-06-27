package org.example.backend.Cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000/","*")
               .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP 메서드 설정
               .allowedHeaders("*") // 허용할 HTTP 헤더 설정
               .exposedHeaders("Authorization", "UserId", "UsersId");// 노출할 헤더 설정
    }
}