# CORS 跨域配置说明

## 🎯 问题描述

当前端页面尝试调用后端 API 时，浏览器报错：
```
strict-origin-when-cross-origin
```

这是由于浏览器的**同源策略**（Same-Origin Policy）限制，阻止了跨域请求。

## ✅ 解决方案

我们实现了**三层 CORS 配置**，确保跨域请求能够正常工作：

### 1. 全局 CORS 过滤器 (`CorsConfig.java`)

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");  // 允许所有来源
        config.addAllowedHeader("*");          // 允许所有请求头
        config.addAllowedMethod("*");          // 允许所有HTTP方法
        config.setAllowCredentials(true);      // 允许携带认证信息
        config.setMaxAge(3600L);              // 预检请求有效期1小时
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
```

### 2. WebMVC 配置 (`WebConfig.java`)

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### 3. Controller 注解 (`MedicalConsultationController.java`)

```java
@RestController
@RequestMapping("/api/consultation")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MedicalConsultationController {
    // ...
}
```

## 🔧 前端配置

前端的 `fetch` 请求也进行了优化：

```javascript
const response = await fetch('/api/consultation/ask', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
    mode: 'cors',                    // 明确指定 CORS 模式
    credentials: 'same-origin',      // 同源请求携带认证信息
    body: JSON.stringify({
        userId: userId,
        message: message
    })
});
```

## 📝 配置说明

### `allowedOriginPatterns("*")` vs `allowedOrigins("*")`

- **`allowedOriginPatterns("*")`**: 支持通配符，可以与 `allowCredentials(true)` 一起使用
- **`allowedOrigins("*")`**: 不支持通配符，与 `allowCredentials(true)` 冲突

### `allowCredentials(true)`

允许前端请求携带以下信息：
- Cookies
- HTTP 认证信息
- TLS 客户端证书

### `maxAge(3600)`

预检请求（OPTIONS）的缓存时间，单位为秒。浏览器会在这个时间内重用预检响应，减少不必要的网络请求。

## ⚠️ 生产环境建议

**当前配置允许所有来源（`*`），仅适用于开发环境！**

### 生产环境配置示例

```java
// 1. 在 CorsConfig.java 中
config.setAllowedOrigins(Arrays.asList(
    "https://yourdomain.com",
    "https://app.yourdomain.com"
));

// 2. 在 WebConfig.java 中
registry.addMapping("/**")
        .allowedOrigins("https://yourdomain.com")
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowCredentials(true);

// 3. 在 Controller 中
@CrossOrigin(origins = "https://yourdomain.com")
```

### 使用环境变量配置

```java
@Value("${cors.allowed-origins:*}")
private String allowedOrigins;

@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOriginPatterns(allowedOrigins)
            // ...
}
```

在 `application.yml` 中：

```yaml
cors:
  allowed-origins: https://yourdomain.com,https://app.yourdomain.com
```

## 🧪 测试 CORS

### 1. 浏览器测试

打开浏览器开发者工具（F12），在 Network 标签中查看请求：

**成功的 CORS 请求响应头应包含：**
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

### 2. 命令行测试

**预检请求（OPTIONS）：**
```bash
curl -X OPTIONS http://localhost:8080/api/consultation/ask \
  -H "Origin: http://localhost:8080" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v
```

**实际请求（POST）：**
```bash
curl -X POST http://localhost:8080/api/consultation/ask \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:8080" \
  -d '{"userId":"test","message":"Hello"}' \
  -v
```

## 🔍 常见问题

### 1. 仍然报 CORS 错误？

**检查清单：**
- ✅ 确认应用已重启
- ✅ 清除浏览器缓存
- ✅ 检查是否有其他 CORS 配置冲突
- ✅ 确认前端请求的 URL 正确
- ✅ 查看浏览器控制台的完整错误信息

### 2. 预检请求失败？

预检请求是浏览器在实际请求前发送的 OPTIONS 请求。

**原因可能：**
- Controller 没有处理 OPTIONS 方法
- Spring Security 拦截了 OPTIONS 请求
- Nginx/代理服务器过滤了 OPTIONS 请求

**解决方法：**
```java
// 如果使用 Spring Security，需要允许 OPTIONS 请求
http.cors().and()
    .authorizeRequests()
    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

### 3. Credentials 相关错误？

**错误信息：**
```
The value of the 'Access-Control-Allow-Origin' header in the response 
must not be the wildcard '*' when the request's credentials mode is 'include'.
```

**解决方法：**
使用 `allowedOriginPatterns("*")` 而不是 `allowedOrigins("*")`

## 📚 参考资料

- [MDN - CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring CORS 文档](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-cors)
- [Spring Boot CORS 配置](https://spring.io/guides/gs/rest-service-cors/)

---

**更新时间**: 2025-11-05  
**配置位置**: 
- `src/main/java/com/yihu/agent/config/CorsConfig.java`
- `src/main/java/com/yihu/agent/config/WebConfig.java`
- `src/main/java/com/yihu/agent/controller/MedicalConsultationController.java`
- `src/main/resources/static/medical-consultation.html`

