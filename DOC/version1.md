# websocket —— langgraph4j + langchain4j 的多轮记忆对话
## 搭建websocket实现基本通信
> 使用STOMP方式
### 步骤
1. 加入依赖
2. 配置websocket
3. 编写controller
4. 创建消息模型类（DTO）

### d

WebSocket 连接端点：`ws://localhost:8080/websocket`

客户端发送消息地址：`/app/send/message`

客户端订阅接收消息地址：`/topic/messages`