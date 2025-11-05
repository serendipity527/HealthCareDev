# WebSocket 聊天室

一个基于 Spring Boot 和 WebSocket 的简单聊天应用。

## 项目简介

这是一个最简单的 WebSocket 聊天室应用,实现了基本的实时消息广播功能。

## 技术栈

- Spring Boot 3.5.7
- Spring WebSocket
- STOMP 协议
- SockJS
- Java 17

## 功能特点

- ✅ WebSocket 实时通信
- ✅ 用户消息显示在右侧（紫色渐变气泡）
- ✅ 服务器消息显示在左侧（白色气泡）
- ✅ 现代化的聊天界面设计
- ✅ 智能AI助手自动回复
- ✅ 连接状态实时显示
- ✅ 自动重连机制
- ✅ 消息时间戳显示
- ✅ 平滑动画效果

## 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.6+

### 运行步骤

1. 克隆项目
```bash
git clone <repository-url>
cd HealthCareDev
```

2. 编译项目
```bash
mvn clean install
```

3. 运行应用
```bash
mvn spring-boot:run
```

4. 访问应用
打开浏览器访问: http://localhost:8080/chat.html

或者直接运行启动脚本:
```bash
start-chat.bat
```

## 使用说明

### 基本操作

1. 打开聊天室后会自动连接到服务器
2. 在底部输入框中输入消息
3. 点击"发送"按钮或按回车键发送消息
4. 你的消息会显示在右侧（紫色气泡），AI助手的回复会显示在左侧（白色气泡）

### AI助手支持的对话

- **打招呼**: 你好、hello、hi
- **询问时间**: 现在几点、时间
- **询问天气**: 天气怎么样
- **表达感谢**: 谢谢、感谢
- **告别**: 再见、拜拜
- **自我介绍**: 你是谁、介绍一下
- **功能查询**: 你能做什么、功能
- **帮助**: 帮助、help
- **其他消息**: 会随机智能回复

### 连接状态

- 🟢 绿点: 已连接
- 🔴 红点: 连接断开（会自动重连）

## 项目结构

```
src/main/java/com/yihu/agent/
├── config/
│   └── StompWebSocketConfig.java      # WebSocket 配置
├── controller/
│   └── WebSocketController.java       # 消息处理控制器
├── dto/
│   └── MessageDTO.java                # 消息数据传输对象
└── HealthCareDevApplication.java      # 应用启动类

src/main/resources/
├── static/
│   └── chat.html                      # 聊天室前端页面
└── application.yml                    # 应用配置文件

DOC/
└── chat-room-guide.md                 # 详细使用指南
```

## WebSocket 端点

- **WebSocket 连接**: `/websocket`
- **消息发送**: `/app/chat`
- **消息订阅**: `/topic/messages`

