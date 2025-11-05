# AI 对话功能配置指南

本项目集成了 LangChain4j，支持通过 WebSocket 进行实时 AI 对话。

## 🚀 快速开始

### 1. 配置 API 密钥

项目支持多种 AI 服务商，选择其中一种配置即可：

#### 方式 A：使用环境变量（推荐）

```bash
# Windows (PowerShell)
$env:AI_API_KEY="your-api-key"
$env:AI_BASE_URL="https://api.openai.com/v1"
$env:AI_MODEL_NAME="gpt-3.5-turbo"

# Windows (CMD)
set AI_API_KEY=your-api-key
set AI_BASE_URL=https://api.openai.com/v1
set AI_MODEL_NAME=gpt-3.5-turbo

# Linux/Mac
export AI_API_KEY="your-api-key"
export AI_BASE_URL="https://api.openai.com/v1"
export AI_MODEL_NAME="gpt-3.5-turbo"
```

#### 方式 B：直接修改 application.yml

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: sk-your-real-api-key-here
      base-url: https://api.openai.com/v1
      model-name: gpt-3.5-turbo
```

### 2. 支持的 AI 服务商

#### OpenAI（官方）

```yaml
api-key: sk-xxx  # 从 https://platform.openai.com/api-keys 获取
base-url: https://api.openai.com/v1
model-name: gpt-3.5-turbo  # 或 gpt-4, gpt-4-turbo
```

#### DeepSeek（国内推荐，价格便宜）

```yaml
api-key: sk-xxx  # 从 https://platform.deepseek.com/api_keys 获取
base-url: https://api.deepseek.com
model-name: deepseek-chat
```

#### 阿里云通义千问

```yaml
api-key: sk-xxx  # 从 https://dashscope.console.aliyun.com/apiKey 获取
base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
model-name: qwen-plus  # 或 qwen-turbo, qwen-max
```

#### Ollama（本地运行，免费）

首先安装并启动 Ollama：
```bash
# 下载: https://ollama.ai/download
# 拉取模型
ollama pull llama2
```

配置：
```yaml
api-key: ollama  # 本地运行不需要真实key
base-url: http://localhost:11434/v1
model-name: llama2  # 或 mistral, codellama 等
```

### 3. 启动应用

```bash
# 确保配置了 API 密钥后启动
mvn spring-boot:run

# 或者使用批处理脚本（Windows）
start-chat.bat
```

### 4. 测试 AI 对话

1. 打开浏览器访问：http://localhost:8080/chat.html
2. 等待连接成功（状态显示"已连接"）
3. 输入消息，即可与 AI 进行对话

## 🛠️ 技术架构

### 核心组件

1. **AiConfig.java** - AI 模型配置类
   - 配置 OpenAI 兼容的 ChatLanguageModel
   - 支持自定义 base-url、model-name、temperature 等参数

2. **AiChatService.java** - AI 聊天服务接口
   - 使用 `@AiService` 注解自动实现
   - 通过 `@SystemMessage` 定义 AI 角色和行为规则

3. **WebSocketController.java** - WebSocket 控制器
   - 接收用户消息
   - 调用 AI 服务生成回复
   - 通过 WebSocket 实时返回给客户端

### 工作流程

```
用户输入消息
    ↓
前端通过 WebSocket 发送到 /app/chat
    ↓
WebSocketController 接收消息
    ↓
调用 AiChatService.chat(userMessage)
    ↓
LangChain4j 调用 AI API 生成回复
    ↓
返回回复到 /topic/messages
    ↓
前端接收并显示 AI 回复
```

## 🎯 高级配置

### 调整 AI 回复风格

编辑 `AiChatService.java` 中的 `@SystemMessage`：

```java
@SystemMessage("""
    你是一个{角色定位}，名字叫{名字}。
    {行为规则}
    """)
String chat(String userMessage);
```

### 配置参数说明

| 参数 | 说明 | 默认值 | 推荐值 |
|------|------|--------|--------|
| temperature | 回复随机性（0-2） | 0.7 | 0.7（平衡），0.3（保守），1.5（创意） |
| max-tokens | 最大回复长度 | 2000 | 2000-4000 |
| timeout | 请求超时（秒） | 60 | 60-120 |

### 记忆功能（聊天历史）

如需实现多轮对话记忆，可以使用 LangChain4j 的 `ChatMemory`：

```java
@AiService
public interface AiChatService {
    String chat(@MemoryId String sessionId, @UserMessage String message);
}
```

## 🔧 故障排查

### 问题1：AI 服务不可用

**症状**：前端收到"AI 服务暂时不可用"提示

**解决方法**：
1. 检查 API 密钥是否正确
2. 检查 base-url 是否可访问
3. 检查网络连接（可能需要代理）
4. 查看控制台日志中的详细错误信息

### 问题2：回复很慢

**解决方法**：
1. 使用国内服务商（如 DeepSeek、阿里云）
2. 减小 max-tokens 参数
3. 使用更快的模型（如 gpt-3.5-turbo）

### 问题3：本地 Ollama 连接失败

**解决方法**：
```bash
# 检查 Ollama 是否运行
ollama list

# 确保模型已下载
ollama pull llama2

# 检查服务是否启动
curl http://localhost:11434/api/tags
```

## 📚 相关文档

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [OpenAI API 文档](https://platform.openai.com/docs)
- [DeepSeek API 文档](https://platform.deepseek.com/api-docs)
- [阿里云通义千问文档](https://help.aliyun.com/zh/dashscope/)
- [Ollama 官方文档](https://ollama.ai/docs)

## 💡 开发建议

1. **开发阶段**：使用 Ollama 本地模型，免费且快速
2. **测试阶段**：使用 DeepSeek，价格便宜（0.001元/千tokens）
3. **生产阶段**：根据需求选择合适的服务商和模型

## 🔐 安全提示

⚠️ **重要**：不要将 API 密钥提交到版本控制系统！

建议：
1. 使用环境变量配置密钥
2. 将 `.env` 文件添加到 `.gitignore`
3. 在生产环境使用密钥管理服务

