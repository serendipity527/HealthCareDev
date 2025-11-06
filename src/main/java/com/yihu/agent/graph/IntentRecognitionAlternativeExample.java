package org.bsc.langgraph4j.examples;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncCommandAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.Command;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.utils.TypeRef;

import java.util.Map;
import java.util.Optional;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 意图识别示例：展示三种不同的方式传递意图信息
 * 
 * 方式1：意图放在状态中（推荐，便于持久化和调试）
 * 方式2：意图放在 config.metadata 中（适合临时数据）
 * 方式3：在路由函数中直接计算（适合简单逻辑）
 */
public class IntentRecognitionAlternativeExample {

    // ========== 状态定义 ==========
    static class SimpleState extends AgentState {
        public static final String INPUT_KEY = "input";
        public static final String RESULT_KEY = "result";
        // 注意：这里不定义 INTENT_KEY，演示不把意图放在状态中

        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                INPUT_KEY, Channels.lastWrite(),
                RESULT_KEY, Channels.lastWrite()
        );

        public SimpleState(Map<String, Object> initData) {
            super(initData);
        }

        public Optional<String> input() {
            return this.<String>value(INPUT_KEY);
        }
    }

    // ========== 节点定义 ==========
    static AsyncNodeAction<SimpleState> intentRecognizeNode = state -> {
        String input = state.input().orElse("未知输入");
        System.out.println("🔍 意图识别节点执行，输入: " + input);
        return completedFuture(Map.of(
                SimpleState.RESULT_KEY, "意图识别完成"
        ));
    };

    static AsyncNodeAction<SimpleState> queryBranch = state -> 
        completedFuture(Map.of(SimpleState.RESULT_KEY, "执行查询操作"));

    static AsyncNodeAction<SimpleState> purchaseBranch = state -> 
        completedFuture(Map.of(SimpleState.RESULT_KEY, "执行购买操作"));

    static AsyncNodeAction<SimpleState> otherBranch = state -> 
        completedFuture(Map.of(SimpleState.RESULT_KEY, "处理其他请求"));

    // ========== 方式1：意图放在状态中（当前示例的做法）==========
    static class StateWithIntent extends SimpleState {
        public static final String INTENT_KEY = "intent";
        
        public static final Map<String, Channel<?>> SCHEMA_WITH_INTENT = Map.of(
                INPUT_KEY, Channels.lastWrite(),
                INTENT_KEY, Channels.lastWrite(),
                RESULT_KEY, Channels.lastWrite()
        );

        public StateWithIntent(Map<String, Object> initData) {
            super(initData);
        }

        public Optional<String> intent() {
            return this.<String>value(INTENT_KEY);
        }
    }

    static AsyncNodeAction<StateWithIntent> intentRecognizeNode1 = state -> {
        String input = state.input().orElse("");
        String intent;
        if (input.contains("查询")) {
            intent = "query";
        } else if (input.contains("购买")) {
            intent = "purchase";
        } else {
            intent = "other";
        }
        return completedFuture(Map.of(StateWithIntent.INTENT_KEY, intent));
    };

    static AsyncCommandAction<StateWithIntent> routeByIntent1 = (state, config) -> {
        String intent = state.intent()
                .orElseThrow(() -> new IllegalStateException("意图未识别"));
        return completedFuture(new Command(intent));
    };

    // ========== 方式2：意图放在 config.metadata 中 ==========
    static AsyncNodeAction<SimpleState> intentRecognizeNode2 = state -> {
        String input = state.input().orElse("");
        String intent;
        if (input.contains("查询")) {
            intent = "query";
        } else if (input.contains("购买")) {
            intent = "purchase";
        } else {
            intent = "other";
        }
        // 注意：这里不更新状态，意图会通过其他方式传递
        System.out.println("识别到意图: " + intent + "（将通过 metadata 传递）");
        return completedFuture(Map.of());
    };

    // 这种方式需要在节点中设置 metadata，但节点无法直接修改 config
    // 所以这种方式更适合在外部设置 metadata，或者通过状态间接传递
    // 实际上，如果要在节点执行后设置 metadata，需要在节点返回的 Command 中处理
    // 但 addConditionalEdges 的路由函数是在节点执行后调用的，此时可以读取 metadata

    // ========== 方式3：在路由函数中直接计算（推荐用于简单逻辑）==========
    static AsyncCommandAction<SimpleState> routeByIntent3 = (state, config) -> {
        // 直接从状态中的输入计算意图，不需要单独存储意图
        String input = state.input().orElse("");
        
        String intent;
        if (input.contains("查询") || input.contains("搜索") || input.contains("query")) {
            intent = "query";
        } else if (input.contains("购买") || input.contains("下单") || input.contains("buy")) {
            intent = "purchase";
        } else {
            intent = "other";
        }
        
        System.out.println("🔀 路由决策（直接计算），当前意图: " + intent);
        
        return completedFuture(new Command(intent));
    };

    // ========== 构建图 ==========
    
    // 方式1：意图在状态中
    public static CompiledGraph<StateWithIntent> buildGraph1() throws GraphStateException {
        return new StateGraph<>(StateWithIntent.SCHEMA_WITH_INTENT, StateWithIntent::new)
                .addNode("intent_recognize", node_async(intentRecognizeNode1))
                .addNode("query_branch", node_async(queryBranch))
                .addNode("purchase_branch", node_async(purchaseBranch))
                .addNode("other_branch", node_async(otherBranch))
                .addEdge(START, "intent_recognize")
                .addConditionalEdges("intent_recognize", routeByIntent1,
                        Map.of("query", "query_branch",
                                "purchase", "purchase_branch",
                                "other", "other_branch"))
                .addEdge("query_branch", END)
                .addEdge("purchase_branch", END)
                .addEdge("other_branch", END)
                .compile();
    }

    // 方式3：在路由函数中直接计算（最简单，不需要存储意图）
    public static CompiledGraph<SimpleState> buildGraph3() throws GraphStateException {
        return new StateGraph<>(SimpleState.SCHEMA, SimpleState::new)
                .addNode("intent_recognize", node_async(intentRecognizeNode))
                .addNode("query_branch", node_async(queryBranch))
                .addNode("purchase_branch", node_async(purchaseBranch))
                .addNode("other_branch", node_async(otherBranch))
                .addEdge(START, "intent_recognize")
                .addConditionalEdges("intent_recognize", routeByIntent3,
                        Map.of("query", "query_branch",
                                "purchase", "purchase_branch",
                                "other", "other_branch"))
                .addEdge("query_branch", END)
                .addEdge("purchase_branch", END)
                .addEdge("other_branch", END)
                .compile();
    }

    // ========== 运行示例 ==========
    public static void main(String[] args) throws GraphStateException {
        System.out.println("========== 方式1：意图放在状态中 ==========");
        var graph1 = buildGraph1();
        var result1 = graph1.stream(Map.of(
                SimpleState.INPUT_KEY, "我想查询商品信息"
        )).stream()
                .peek(output -> System.out.println("节点: " + output.nodeId()))
                .reduce((a, b) -> b)
                .map(NodeOutput::state);
        result1.ifPresent(state -> {
            System.out.println("最终结果: " + state.<String>value("result").orElse("无结果"));
            System.out.println("识别意图: " + state.intent().orElse("无意图"));
        });

        System.out.println("\n========== 方式3：在路由函数中直接计算 ==========");
        var graph3 = buildGraph3();
        var result3 = graph3.stream(Map.of(
                SimpleState.INPUT_KEY, "我要购买这个商品"
        )).stream()
                .peek(output -> System.out.println("节点: " + output.nodeId()))
                .reduce((a, b) -> b)
                .map(NodeOutput::state);
        result3.ifPresent(state -> {
            System.out.println("最终结果: " + state.<String>value("result").orElse("无结果"));
            System.out.println("注意：意图没有存储在状态中，只在路由时计算");
        });
    }
}

