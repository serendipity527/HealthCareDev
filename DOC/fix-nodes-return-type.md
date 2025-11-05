# 修复节点返回类型

## 问题
所有节点方法的返回类型需要从 `Map<String, Object>` 改为 `CompletableFuture<Map<String, Object>>`

## 需要修改的节点

1. IntentRouterNode
2. EmergencyResponseNode  
3. GeneralChatNode
4. InformationGatheringNode
5. SafetyCheckAndRecommendationNode
6. SaveSummaryNode

## 修改模式

### 修改前
```java
public Map<String, Object> process(MedicalConsultationState state) {
    // ... 处理逻辑
    return Map.of("state", state);
}
```

### 修改后
```java
import java.util.concurrent.CompletableFuture;

public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
    // ... 处理逻辑
    return CompletableFuture.completedFuture(state.data());
}
```

## 关键点

1. 添加import: `import java.util.concurrent.CompletableFuture;`
2. 返回类型: `CompletableFuture<Map<String, Object>>`
3. 返回语句: `return CompletableFuture.completedFuture(state.data());`
4. 不再使用 `Map.of("state", state)`，而是直接使用 `state.data()`

## 其他修改

### MedicalConsultationGraph
- 使用显式类型声明: `StateGraph<MedicalConsultationState> workflow = ...`
- lambda表达式需要接受 `RunnableConfig` 参数

### MedicalConsultationService  
- invoke参数: `compiledGraph.invoke(initialState.data())`
- 不是 `compiledGraph.invoke(initialState)`

