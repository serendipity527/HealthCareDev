# addConditionalEdges 方法使用说明

## 方法签名

```java
public StateGraph<State> addConditionalEdges(
    String sourceId,                    // 源节点ID
    AsyncCommandAction<State> condition, // 条件判断逻辑，返回 Command
    Map<String, String> mappings        // 条件值到目标节点的映射
)
```

## 工作原理

1. **源节点执行完成后**，会调用 `condition` 函数
2. **condition 函数**接收当前状态，返回一个 `Command` 对象
3. **Command.gotoNode()** 返回的字符串会匹配到 `mappings` 中的 key
4. **根据匹配的 key**，跳转到 `mappings` 中对应的目标节点

## Command 类说明

```java
// Command 构造函数
new Command(String gotoNode)                    // 只指定下一个节点
new Command(String gotoNode, Map<String,Object> update)  // 指定节点和状态更新
new Command(Map<String,Object> update)         // 只更新状态，不跳转
```

## 完整示例：START -> 意图识别 -> 三个分支 -> END

```java
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.*;
import org.bsc.langgraph4j.state.*;
import static org.bsc.langgraph4j.StateGraph.*;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static java.util.concurrent.CompletableFuture.completedFuture;

// 1. 定义状态
class IntentState extends AgentState {
    public static final String INTENT_KEY = "intent";
    public static final String INPUT_KEY = "input";
    
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
        INTENT_KEY, Channels.lastWrite(),
        INPUT_KEY, Channels.lastWrite()
    );
    
    public IntentState(Map<String, Object> initData) {
        super(initData);
    }
}

// 2. 定义节点
AsyncNodeAction<IntentState> intentRecognizeNode = state -> {
    String input = state.<String>value("input").orElse("");
    String intent;
    
    if (input.contains("查询")) {
        intent = "query";
    } else if (input.contains("购买")) {
        intent = "purchase";
    } else {
        intent = "other";
    }
    
    return completedFuture(Map.of("intent", intent));
};

AsyncNodeAction<IntentState> queryBranch = state -> 
    completedFuture(Map.of("result", "执行查询"));

AsyncNodeAction<IntentState> purchaseBranch = state -> 
    completedFuture(Map.of("result", "执行购买"));

AsyncNodeAction<IntentState> otherBranch = state -> 
    completedFuture(Map.of("result", "处理其他"));

// 3. 定义路由函数（使用 AsyncCommandAction）
AsyncCommandAction<IntentState> routeByIntent = (state, config) -> {
    String intent = state.<String>value("intent")
        .orElseThrow(() -> new IllegalStateException("意图未识别"));
    
    // 返回 Command，gotoNode 必须是 mappings 中的 key
    return completedFuture(new Command(intent));
};

// 4. 构建图
var graph = new StateGraph<>(IntentState.SCHEMA, IntentState::new)
    .addNode("intent_recognize", node_async(intentRecognizeNode))
    .addNode("query_branch", node_async(queryBranch))
    .addNode("purchase_branch", node_async(purchaseBranch))
    .addNode("other_branch", node_async(otherBranch))
    
    // START -> 意图识别
    .addEdge(START, "intent_recognize")
    
    // 意图识别 -> 三个分支（条件边）
    .addConditionalEdges(
        "intent_recognize",        // 源节点
        routeByIntent,             // 路由函数：返回 Command("query") 或 Command("purchase") 或 Command("other")
        Map.of(
            "query", "query_branch",      // Command.gotoNode() == "query" -> 跳转到 query_branch
            "purchase", "purchase_branch", // Command.gotoNode() == "purchase" -> 跳转到 purchase_branch
            "other", "other_branch"       // Command.gotoNode() == "other" -> 跳转到 other_branch
        )
    )
    
    // 三个分支都到 END
    .addEdge("query_branch", END)
    .addEdge("purchase_branch", END)
    .addEdge("other_branch", END)
    
    .compile();

// 5. 运行
graph.stream(Map.of("input", "我想查询商品"))
    .forEach(System.out::println);
```

## 关键点总结

1. **路由函数必须返回 Command**：`Command` 的 `gotoNode()` 方法返回的字符串必须匹配 `mappings` 中的 key
2. **mappings 的 key**：对应路由函数返回的 Command.gotoNode() 值
3. **mappings 的 value**：对应目标节点的名称
4. **可以更新状态**：Command 可以同时更新状态，例如 `new Command("query", Map.of("step", 1))`

## 图结构

```
START
  ↓
意图识别节点 (intent_recognize)
  ↓ (条件边)
  ├─→ query_branch ─→ END
  ├─→ purchase_branch ─→ END
  └─→ other_branch ─→ END
```

