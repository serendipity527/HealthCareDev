package com.yihu.agent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置 CORS、静态资源等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有来源（开发环境）
                // 生产环境应该设置具体的域名
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带认证信息
                .allowCredentials(true)
                // 预检请求的有效期（秒）
                .maxAge(3600);
    }
}

