# 医疗咨询状态图系统

基于 **LangGraph4j** 实现的智能医疗咨询系统，支持意图识别、动态风险评估和智能信息收集。

## 🎯 核心特性

- ✅ **智能意图路由**：自动识别高危医疗、一般咨询、日常聊天
- ✅ **动态风险评估**：实时评估症状风险，及时响应高危情况
- ✅ **循环信息收集**：智能提问，全面了解用户症状
- ✅ **安全检查机制**：多重安全检查，确保建议合理
- ✅ **病历自动生成**：完整记录咨询过程和建议
- ✅ **美观Web界面**：现代化UI设计，良好用户体验

## 📊 状态图流程

```
START → Initial → IntentRouter
                     ├─→ [高危] → EmergencyResponse → END
                     ├─→ [聊天] → GeneralChat → END
                     └─→ [医疗] → InformationGathering ⟲
                                    ├─→ [风险升级] → EmergencyResponse → END
                                    └─→ [低危] → SafetyCheck → SaveSummary → END
```

## 🚀 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- OpenAI API密钥（或兼容的API服务）

### 2. 配置API密钥

编辑 `src/main/resources/application.yml`：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: your-api-key-here
      base-url: https://api.openai.com/v1
      model-name: gpt-3.5-turbo
```

支持的AI服务：
- OpenAI: `https://api.openai.com/v1`
- DeepSeek: `https://api.deepseek.com`
- 阿里云通义千问: `https://dashscope.aliyuncs.com/compatible-mode/v1`
- Ollama本地: `http://localhost:11434/v1`

### 3. 启动应用

#### Windows用户
```bash
start-medical-consultation.bat
```

#### Linux/Mac用户
```bash
mvn clean install
mvn spring-boot:run
```

### 4. 访问系统

浏览器打开：
- 医疗咨询页面: http://localhost:8080/medical-consultation.html
- API接口: http://localhost:8080/api/consultation/ask

## 🧪 测试API

### 使用测试脚本（Windows）

```bash
test-api.bat
```

### 手动测试

#### 测试1：高危医疗情况
```bash
curl -X POST http://localhost:8080/api/consultation/ask \
  -H "Content-Type: application/json" \
  -d '{"userId":"user001","message":"我突然感到剧烈胸痛，无法呼吸"}'
```

预期：立即触发紧急响应，提供急救指引

#### 测试2：一般医疗咨询
```bash
curl -X POST http://localhost:8080/api/consultation/ask \
  -H "Content-Type: application/json" \
  -d '{"userId":"user002","message":"我最近总是头疼"}'
```

预期：进入信息收集流程，询问相关症状

#### 测试3：通用聊天
```bash
curl -X POST http://localhost:8080/api/consultation/ask \
  -H "Content-Type: application/json" \
  -d '{"userId":"user003","message":"你好，今天天气真好"}'
```

预期：友好的日常对话回复

## 📁 项目结构

```
src/main/java/com/yihu/agent/
├── graph/
│   ├── enums/                  # 枚举类型
│   │   ├── IntentType.java    # 意图类型
│   │   └── RiskLevel.java     # 风险等级
│   ├── nodes/                  # 状态图节点
│   │   ├── InitialNode.java
│   │   ├── IntentRouterNode.java
│   │   ├── EmergencyResponseNode.java
│   │   ├── GeneralChatNode.java
│   │   ├── InformationGatheringNode.java
│   │   ├── SafetyCheckAndRecommendationNode.java
│   │   └── SaveSummaryNode.java
│   ├── state/                  # 状态类
│   │   └── MedicalConsultationState.java
│   └── MedicalConsultationGraph.java  # 状态图配置
├── service/
│   └── MedicalConsultationService.java  # 业务服务
├── controller/
│   └── MedicalConsultationController.java  # REST API
└── config/
    └── LangChain4jConfig.java  # LangChain4j配置
```

## 📖 详细文档

- [完整使用指南](DOC/medical-consultation-graph.md)
- [实现总结](DOC/医疗咨询状态图实现总结.md)

## 🎨 Web界面预览

访问 http://localhost:8080/medical-consultation.html

界面特点：
- 💬 实时对话
- 🎯 快速咨询按钮
- ⚠️ 紧急提醒高亮
- 📱 响应式设计

## 🔧 常见问题

### Q1: 编译错误 - ChatLanguageModel找不到

**解决方案**：
```bash
mvn clean install -U
```

强制更新Maven依赖

### Q2: API调用超时

**解决方案**：
在 `application.yml` 中增加超时时间：
```yaml
langchain4j:
  open-ai:
    chat-model:
      timeout: 120
```

### Q3: LLM响应不准确

**解决方案**：
- 调整temperature参数（降低可提高稳定性）
- 优化节点中的Prompt内容
- 考虑使用更强大的模型（如GPT-4）

## 🛠️ 扩展开发

### 添加新节点

1. 创建节点类（继承基础结构）
2. 在 `MedicalConsultationGraph` 中注册节点
3. 配置节点间的边

### 集成数据库

在 `SaveSummaryNode` 中添加持久化逻辑：

```java
@Autowired
private MedicalRecordRepository repository;

// 保存到数据库
repository.save(new MedicalRecord(state));
```

### 集成RAG

在 `SafetyCheckAndRecommendationNode` 中集成：

```java
@Autowired
private EmbeddingStore embeddingStore;

// 查询相关医疗知识
List<EmbeddingMatch> matches = embeddingStore.findRelevant(...);
```

## 📊 技术栈

- **框架**: Spring Boot 3.5.7
- **AI**: LangChain4j 1.7.1-beta14
- **状态图**: LangGraph4j 1.7.1
- **LLM**: OpenAI API兼容
- **前端**: 原生HTML/CSS/JavaScript

## 📝 License

本项目仅供学习和研究使用。

## ⚠️ 免责声明

本系统提供的医疗建议仅供参考，不能替代专业医疗诊断。如有严重症状，请立即就医。

## 🙋 支持

如有问题或建议，请查看项目文档或提交Issue。

---

**项目状态**: ✅ 可用于开发和测试

**最后更新**: 2025-11-05

