# 故障排除指南

## ❌ 常见错误：404 (Not Found) - WebSocket 连接失败

### 错误信息
```
GET http://localhost:63342/websocket/info?t=... 404 (Not Found)
Whoops! Lost connection to http://localhost:63342/websocket
```

### 原因分析
这个错误表示你通过 **IntelliJ IDEA 的内置服务器**（端口 63342）打开了 HTML 文件，而不是通过 Spring Boot 应用（端口 8080）访问。

### ✅ 正确的访问方式

#### 步骤 1：确保 Spring Boot 应用正在运行
```bash
# 在项目根目录执行
mvn spring-boot:run

# 等待看到类似以下输出：
# Tomcat started on port(s): 8080 (http)
# Started HealthCareDevApplication in X seconds
```

#### 步骤 2：通过正确的 URL 访问
**正确 ✅**：
```
http://localhost:8080/chat.html
```

**错误 ❌**：
```
http://localhost:63342/...  (IDEA 内置服务器)
file:///D:/Project/...      (直接打开文件)
```

### 如何操作

1. **启动 Spring Boot 应用**
   - 方法1：运行 `start-chat.bat`
   - 方法2：执行 `mvn spring-boot:run`
   - 方法3：在 IDEA 中运行 `HealthCareDevApplication` 主类

2. **等待应用完全启动**
   查看控制台输出，确认看到：
   ```
   Tomcat started on port(s): 8080 (http)
   ```

3. **在浏览器中访问**
   直接在浏览器地址栏输入：
   ```
   http://localhost:8080/chat.html
   ```

4. **验证连接成功**
   - 页面顶部状态栏显示 🟢 "已连接"
   - 输入框和发送按钮可以使用
   - 可以正常发送和接收消息

## 🔍 其他常见问题

### 问题 1：端口 8080 被占用

**错误信息**：
```
Port 8080 was already in use.
```

**解决方案**：
1. 方法1：修改应用端口
```yaml
# 编辑 src/main/resources/application.yml
server:
  port: 8081  # 改为其他端口
```

2. 方法2：关闭占用端口的程序
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# 或使用任务管理器关闭
```

### 问题 2：Maven 构建失败

**错误信息**：
```
[ERROR] Failed to execute goal...
```

**解决方案**：
1. 检查 JDK 版本（需要 17 或更高）
```bash
java -version
```

2. 清理并重新构建
```bash
mvn clean install -U
```

3. 检查网络连接（Maven 需要下载依赖）

### 问题 3：浏览器控制台显示 CORS 错误

**错误信息**：
```
Access to XMLHttpRequest... has been blocked by CORS policy
```

**解决方案**：
配置文件中已经设置了允许所有源（`setAllowedOriginPatterns("*")`），但如果仍有问题，检查：

1. 确保通过 `http://localhost:8080` 访问
2. 不要通过 `file://` 协议打开
3. 不要通过其他端口访问

### 问题 4：页面空白或样式错误

**可能原因**：
- CDN 资源加载失败
- 网络连接问题

**解决方案**：
1. 检查浏览器控制台（F12）的网络选项卡
2. 确保可以访问：
   - `https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js`
   - `https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js`

### 问题 5：消息发送后无回复

**检查清单**：
1. ✅ WebSocket 连接状态为"已连接"（绿点）
2. ✅ 浏览器控制台无错误信息
3. ✅ Spring Boot 后端正在运行
4. ✅ 后端控制台无异常日志

**调试步骤**：
1. 打开浏览器开发者工具（F12）
2. 查看 Console 选项卡的日志
3. 查看 Network 选项卡的 WS（WebSocket）连接
4. 检查后端控制台日志

## 📋 快速检查清单

在遇到问题时，按顺序检查：

- [ ] Spring Boot 应用是否正在运行？
- [ ] 端口 8080 是否可用？
- [ ] 访问地址是否为 `http://localhost:8080/chat.html`？
- [ ] 浏览器控制台是否有错误？
- [ ] 网络是否正常（CDN 资源能否加载）？

## 🛠️ 高级调试

### 查看 WebSocket 连接详情

1. 打开浏览器开发者工具（F12）
2. 切换到 "Network" 选项卡
3. 刷新页面
4. 查找 "websocket" 或 "sockjs" 相关的请求
5. 点击查看详细信息和消息帧

### 启用详细日志

在 `application.yml` 中添加：
```yaml
logging:
  level:
    org.springframework.web.socket: DEBUG
    org.springframework.messaging: DEBUG
```

### 测试 WebSocket 端点

使用浏览器访问：
```
http://localhost:8080/websocket/info
```

应该返回类似：
```json
{
  "websocket": true,
  "origins": ["*:*"],
  "cookie_needed": false,
  "entropy": 123456789
}
```

## 💡 最佳实践

1. **总是通过 Spring Boot 访问**
   - 不要直接打开 HTML 文件
   - 不要通过 IDEA 内置服务器打开
   - 使用 `http://localhost:8080/chat.html`

2. **确认应用状态**
   - 启动应用后等待完全启动
   - 查看控制台确认端口信息

3. **使用浏览器开发者工具**
   - F12 打开开发者工具
   - 查看 Console 和 Network 选项卡
   - 有助于快速定位问题

4. **保持依赖更新**
   - 定期执行 `mvn clean install`
   - 确保所有依赖都已正确下载

## 📞 仍然无法解决？

如果按照以上步骤仍然无法解决问题，请检查：

1. **系统环境**
   - JDK 版本：17+
   - Maven 版本：3.6+
   - 操作系统：Windows/Linux/Mac

2. **收集以下信息**
   - 浏览器控制台完整错误信息
   - Spring Boot 后端完整错误日志
   - 访问的完整 URL
   - 系统环境信息

3. **参考文档**
   - [快速开始指南](./quick-start.md)
   - [详细使用指南](WebSocket%20聊天室使用指南.md)
   - [README.md](../README.md)

