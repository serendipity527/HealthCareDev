# WebSocket 消息解析测试指南

## 后端返回数据格式

后端 `MessageDTO` 包含三个字段：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private String sender;          // 发送者
    private String content;         // 消息内容
    private LocalDateTime timestamp; // 发送时间
}
```

## 前端解析实现

前端已完整实现对这三个字段的解析和显示：

### 1. JSON 解析
```javascript
const msg = JSON.parse(message.body);
// 提取字段：
// - msg.sender    (发送者)
// - msg.content   (消息内容)
// - msg.timestamp (时间戳，ISO 8601格式字符串)
```

### 2. 时间戳处理
```javascript
// LocalDateTime 会被序列化为: "2024-01-01T12:00:00"
const date = new Date(message.timestamp);
const time = date.toLocaleTimeString('zh-CN'); // 转换为本地时间格式
```

### 3. 界面显示
- **发送者名称**: 显示在消息顶部（蓝色粗体）
- **消息内容**: 显示在消息主体
- **时间戳**: 显示在消息底部（灰色小字）

## 测试步骤

### 第一步：启动应用

```bash
# 在项目根目录执行
mvn spring-boot:run
```

等待看到类似输出：
```
Started HealthCareDevApplication in X.XXX seconds
```

### 第二步：打开浏览器

1. 访问: http://localhost:8080
2. 按 F12 打开开发者工具（查看控制台日志）
3. 点击界面上的"调试面板"按钮（查看解析过程）

### 第三步：连接并测试

1. **输入用户名**: 例如 "用户A"
2. **点击"连接"按钮**
3. **观察控制台输出**:
   ```
   🔗 WebSocket 连接成功!
   📋 连接信息: [StompFrame对象]
   👤 当前用户: 用户A
   ✅ 连接成功！开始监听后端消息...
   📡 已订阅消息频道: /topic/messages
   ```

### 第四步：发送消息

1. **输入消息**: 例如 "你好，这是测试消息"
2. **点击"发送消息"按钮**
3. **观察控制台日志**:
   ```
   📤 准备发送消息到后端: {sender: "用户A", content: "你好，这是测试消息"}
      - 目标地址: /app/chat
      - 消息内容(JSON): {"sender":"用户A","content":"你好，这是测试消息"}
   ✅ 消息已发送到服务器
   ```

### 第五步：接收并解析消息

控制台会显示完整的解析过程：

```
📩 收到后端原始消息: [StompMessage对象]
📄 消息体(JSON字符串): {"sender":"用户A","content":"你好，这是测试消息","timestamp":"2024-01-01T12:00:00"}
✅ 解析后的消息对象: {sender: "用户A", content: "你好，这是测试消息", timestamp: "2024-01-01T12:00:00"}
   - 发送者: 用户A
   - 内容: 你好，这是测试消息
   - 时间戳: 2024-01-01T12:00:00
🎨 开始渲染消息到界面
   👤 发送者: 用户A
   💬 内容: 你好，这是测试消息
   ⏰ 时间戳解析: 2024-01-01T12:00:00 => 12:00:00
✅ 消息已成功渲染到界面
```

### 第六步：查看界面显示

在聊天界面会看到消息框，包含：

```
┌─────────────────────────────┐
│ 用户A                        │ ← sender (蓝色粗体)
│ 你好，这是测试消息            │ ← content (黑色正文)
│ 12:00:00                    │ ← timestamp (灰色小字)
└─────────────────────────────┘
```

### 第七步：查看调试面板

点击"调试面板"按钮，会显示：

```
🔍 后端返回数据解析                    [清空]
┌──────────────────────────────────────┐
│ ⏰ 12:00:00 - 收到后端消息              │
│ 📄 原始 JSON 字符串:                   │
│ {"sender":"用户A","content":"你好，这是│
│ 测试消息","timestamp":"2024-01-01T12:00│
│ :00"}                                 │
│                                       │
│ ✅ 解析后的对象:                       │
│ • sender: 用户A                       │
│ • content: 你好，这是测试消息           │
│ • timestamp: 2024-01-01T12:00:00      │
└──────────────────────────────────────┘
```

## 多用户测试

### 同时打开两个浏览器窗口

**窗口1:**
- 用户名: "张三"
- 发送: "大家好！"

**窗口2:**  
- 用户名: "李四"
- 发送: "你好张三！"

**预期结果:**
- 两个窗口都能看到对方的消息
- 每条消息都正确显示发送者、内容和时间

## 验证清单

- [ ] ✅ WebSocket 连接成功
- [ ] ✅ 消息发送成功
- [ ] ✅ 后端返回 JSON 格式正确
- [ ] ✅ 前端成功解析 sender 字段
- [ ] ✅ 前端成功解析 content 字段
- [ ] ✅ 前端成功解析 timestamp 字段
- [ ] ✅ 时间戳正确转换为本地时间
- [ ] ✅ 消息正确显示在聊天界面
- [ ] ✅ 调试面板正确显示解析信息
- [ ] ✅ 控制台日志输出完整
- [ ] ✅ 多用户消息互通

## 常见问题

### Q1: 控制台显示 "连接失败"
**解决方案:**
- 确认后端应用已启动
- 检查端口 8080 是否被占用
- 查看后端日志是否有错误

### Q2: 收到消息但界面不显示
**解决方案:**
- 查看控制台是否有 JSON 解析错误
- 检查调试面板显示的原始 JSON 格式
- 确认后端返回的字段名与前端一致

### Q3: 时间显示不正确
**解决方案:**
- LocalDateTime 应该被序列化为 ISO 8601 格式
- 确认 Spring Boot 的 Jackson 配置正确
- 检查控制台的时间戳解析日志

## 后端 JSON 序列化配置

确保 Spring Boot 正确序列化 LocalDateTime：

```yaml
# application.yml
spring:
  jackson:
    serialization:
      write-dates-as-timestamps: false
    date-format: yyyy-MM-dd'T'HH:mm:ss
```

或者使用默认配置，Spring Boot 会自动将 LocalDateTime 序列化为 ISO 8601 格式。

## 总结

前端完整实现了对后端 `MessageDTO` 的解析：

| 后端字段 | 类型 | 前端解析 | 界面显示 |
|---------|------|---------|---------|
| sender | String | msg.sender | 消息顶部（蓝色粗体） |
| content | String | msg.content | 消息主体（黑色正文） |
| timestamp | LocalDateTime | msg.timestamp → Date → 本地时间 | 消息底部（灰色小字）|

**所有数据都正确解析并显示在聊天界面中！** ✅

