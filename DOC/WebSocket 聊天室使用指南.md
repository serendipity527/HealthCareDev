# WebSocket 聊天室使用指南

## 功能说明

这是一个基于 Spring Boot WebSocket 的简单聊天室应用，实现了用户与服务器之间的实时对话。

### 主要特性

- ✅ 实时双向通信（WebSocket + STOMP）
- ✅ 现代化的聊天界面
- ✅ 用户消息显示在右侧（紫色渐变气泡）
- ✅ 服务器消息显示在左侧（白色气泡）
- ✅ 自动重连机制
- ✅ 连接状态显示
- ✅ 消息时间戳
- ✅ 智能回复功能

## 启动步骤

### 1. 启动后端服务

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者先打包再运行
mvn clean package
java -jar target/healthcare-dev-0.0.1-SNAPSHOT.jar
```

### 2. 访问聊天室

打开浏览器访问：
```
http://localhost:8080/chat.html
```

## 使用说明

### 基本操作

1. **发送消息**：
   - 在输入框中输入消息
   - 点击"发送"按钮或按回车键发送
   - 你的消息会显示在右侧（紫色气泡）

2. **查看回复**：
   - 服务器的回复会自动显示在左侧（白色气泡）
   - 每条消息都会显示发送时间

3. **连接状态**：
   - 绿点：已连接
   - 红点：连接断开（会自动重连）

### AI助手支持的对话

服务器会根据你的消息内容智能回复：

- **打招呼**：你好、hello、hi
- **询问时间**：现在几点、时间
- **询问天气**：天气怎么样
- **表达感谢**：谢谢、感谢
- **告别**：再见、拜拜
- **自我介绍**：你是谁、介绍一下
- **功能查询**：你能做什么、功能
- **帮助**：帮助、help
- **其他消息**：会随机回复

## 技术架构

### 后端技术栈
- Spring Boot 3.x
- Spring WebSocket
- STOMP 协议
- SockJS（兼容性支持）

### 前端技术栈
- 原生 HTML/CSS/JavaScript
- SockJS Client
- STOMP.js

### WebSocket 配置

- **端点地址**：`/websocket`
- **应用前缀**：`/app`
- **消息代理前缀**：`/topic`
- **发送消息地址**：`/app/chat`
- **订阅消息地址**：`/topic/messages`

## 界面特点

### 视觉设计
- 渐变紫色主题
- 圆角气泡对话框
- 平滑的动画效果
- 响应式布局

### 交互体验
- 消息淡入动画
- 自动滚动到最新消息
- 输入框自动聚焦
- 禁用状态提示

## 扩展功能建议

如果想要增强聊天室功能，可以考虑：

1. **用户系统**：
   - 用户登录/注册
   - 用户昵称和头像
   - 在线用户列表

2. **消息增强**：
   - 消息撤回
   - 图片/文件发送
   - Emoji 表情支持
   - 消息已读状态

3. **聊天室管理**：
   - 多个聊天室/频道
   - 私聊功能
   - 群组聊天

4. **AI 功能增强**：
   - 接入真实 AI API（如 OpenAI）
   - 上下文记忆
   - 更智能的对话

5. **消息持久化**：
   - 数据库存储历史消息
   - 离线消息推送

## 常见问题

### Q: 连接失败怎么办？
A: 确保后端服务已启动（端口 8080），并检查浏览器控制台的错误信息。

### Q: 消息发送后没有回复？
A: 检查浏览器控制台是否有错误，确认 WebSocket 连接状态是"已连接"（绿点）。

### Q: 可以同时打开多个聊天窗口吗？
A: 可以，每个窗口都会独立连接到服务器，并接收所有广播的消息。

### Q: 如何修改端口？
A: 修改 `application.yml` 中的 `server.port` 配置。

## 项目结构

```
src/
├── main/
│   ├── java/com/yihu/agent/
│   │   ├── config/
│   │   │   └── StompWebSocketConfig.java      # WebSocket配置
│   │   ├── controller/
│   │   │   └── WebSocketController.java       # 消息处理器
│   │   ├── dto/
│   │   │   └── MessageDTO.java                # 消息传输对象
│   │   └── HealthCareDevApplication.java      # 应用入口
│   └── resources/
│       ├── static/
│       │   └── chat.html                       # 聊天室页面
│       └── application.yml                     # 应用配置
```

## 开发者信息

- 项目：HealthCareDev
- WebSocket 聊天室模块
- 基于 Spring Boot + STOMP + SockJS

