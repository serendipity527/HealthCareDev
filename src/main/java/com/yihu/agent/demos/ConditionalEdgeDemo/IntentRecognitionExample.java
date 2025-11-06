//package com.yihu.agent.demos.ConditionalEdgeDemo;
//
//import org.bsc.langgraph4j.CompiledGraph;
//import org.bsc.langgraph4j.GraphStateException;
//import org.bsc.langgraph4j.NodeOutput;
//import org.bsc.langgraph4j.StateGraph;
//import org.bsc.langgraph4j.action.AsyncCommandAction;
//import org.bsc.langgraph4j.action.AsyncNodeAction;
//import org.bsc.langgraph4j.action.Command;
//import org.bsc.langgraph4j.state.AgentState;
//import org.bsc.langgraph4j.state.Channel;
//import org.bsc.langgraph4j.state.Channels;
//import java.util.Map;
//import java.util.Optional;
//import static java.util.concurrent.CompletableFuture.completedFuture;
//import static org.bsc.langgraph4j.StateGraph.END;
//import static org.bsc.langgraph4j.StateGraph.START;
//
///**
// * 意图识别示例：START -> 意图识别 -> 三个分支 -> END
// *
// * 演示如何使用 addConditionalEdges 方法添加条件边
// */
//public class IntentRecognitionExample {
//
//    // ========== 1. 定义状态类 ==========
//    static class IntentState extends AgentState {
//        public static final String INTENT_KEY = "intent";
//        public static final String INPUT_KEY = "input";
//        public static final String RESULT_KEY = "result";
//
//        // 定义状态模式
//        public static final Map<String, Channel<?>> SCHEMA = Map.of(
//                INTENT_KEY, Channels.base(() -> null),  // 字符串类型，默认值为null
//                INPUT_KEY, Channels.base(() -> null),
//                RESULT_KEY, Channels.base(() -> null)
//        );
//
//        public IntentState(Map<String, Object> initData) {
//            super(initData);
//        }
//
//        public Optional<String> intent() {
//            return this.<String>value(INTENT_KEY);
//        }
//
//        public Optional<String> input() {
//            return this.<String>value(INPUT_KEY);
//        }
//
//        public Optional<String> result() {
//            return this.<String>value(RESULT_KEY);
//        }
//    }
//
//    // ========== 2. 定义节点 ==========
//
//    /**
//     * 意图识别节点：分析输入并识别意图
//     */
//    static AsyncNodeAction<IntentState> intentRecognizeNode = state -> {
//        String input = state.input().orElse("未知输入");
//        System.out.println("🔍 意图识别节点执行，输入: " + input);
//
//        // 简单的意图识别逻辑
//        String intent;
//        if (input.contains("查询") || input.contains("搜索") || input.contains("query")) {
//            intent = "query";
//        } else if (input.contains("购买") || input.contains("下单") || input.contains("buy")) {
//            intent = "purchase";
//        } else {
//            intent = "other";
//        }
//
//        System.out.println("✅ 识别到的意图: " + intent);
//
//        return completedFuture(Map.of(
//                IntentState.INTENT_KEY, intent,
//                IntentState.RESULT_KEY, "意图识别完成: " + intent
//        ));
//    };
//
//    /**
//     * 查询分支节点
//     */
//    static AsyncNodeAction<IntentState> queryBranchNode = state -> {
//        System.out.println("📊 查询分支节点执行");
//        String result = "执行查询操作: " + state.input().orElse("");
//        return completedFuture(Map.of(
//                IntentState.RESULT_KEY, result
//        ));
//    };
//
//    /**
//     * 购买分支节点
//     */
//    static AsyncNodeAction<IntentState> purchaseBranchNode = state -> {
//        System.out.println("🛒 购买分支节点执行");
//        String result = "执行购买操作: " + state.input().orElse("");
//        return completedFuture(Map.of(
//                IntentState.RESULT_KEY, result
//        ));
//    };
//
//    /**
//     * 其他分支节点
//     */
//    static AsyncNodeAction<IntentState> otherBranchNode = state -> {
//        System.out.println("❓ 其他分支节点执行");
//        String result = "处理其他请求: " + state.input().orElse("");
//        return completedFuture(Map.of(
//                IntentState.RESULT_KEY, result
//        ));
//    };
//
//    // ========== 3. 定义条件路由函数 ==========
//
//    /**
//     * 使用 AsyncCommandAction 定义条件路由
//     *
//     * addConditionalEdges 方法签名：
//     * public StateGraph<State> addConditionalEdges(
//     *     String sourceId,                    // 源节点ID
//     *     AsyncCommandAction<State> condition, // 条件判断逻辑，返回 Command
//     *     Map<String, String> mappings        // 条件值到目标节点的映射
//     * )
//     *
//     * Command 包含：
//     * - gotoNode: 路由函数返回的字符串（对应 mappings 的 key）
//     * - update: 可选的状态更新（Map<String, Object>）
//     *
//     * 注意：意图不一定必须放在状态中！
//     * 路由函数接收 (state, config) 两个参数，你可以：
//     * 1. 从状态中读取意图（当前示例的做法，推荐用于需要持久化的场景）
//     * 2. 从 config.metadata 中读取（适合临时数据）
//     * 3. 在路由函数中直接计算（适合简单逻辑，不需要存储意图）
//     *
//     * 示例3（直接计算）：
//     * static AsyncCommandAction<SimpleState> routeByIntent = (state, config) -> {
//     *     String input = state.input().orElse("");
//     *     String intent = input.contains("查询") ? "query" :
//     *                     input.contains("购买") ? "purchase" : "other";
//     *     return completedFuture(new Command(intent));
//     * };
//     */
//    static AsyncCommandAction<IntentState> routeByIntent = (state, config) -> {
//        // 方式1：从状态中读取意图（当前示例）
//        String intent = state.intent()
//                .orElseThrow(() -> new IllegalStateException("意图未识别"));
//
//        System.out.println("🔀 路由决策，当前意图: " + intent);
//
//        // 返回 Command，gotoNode 必须是 mappings 中的 key
//        // 这里返回的字符串会匹配到 mappings 中对应的目标节点
//        return completedFuture(new Command(intent));
//    };
//
//    // ========== 4. 构建图 ==========
//    public static CompiledGraph<IntentState> buildGraph() throws GraphStateException {
//        return new StateGraph<>(IntentState.SCHEMA, IntentState::new)
//                // 添加节点
//                .addNode("intent_recognize", intentRecognizeNode)
//                .addNode("query_branch", queryBranchNode)
//                .addNode("purchase_branch", purchaseBranchNode)
//                .addNode("other_branch", otherBranchNode)
//
//                // START -> 意图识别
//                .addEdge(START, "intent_recognize")
//
//                // 意图识别 -> 三个分支（条件边）
//                // 路由函数返回的字符串（"query", "purchase", "other"）
//                // 会匹配到 mappings 中对应的目标节点
//                .addConditionalEdges(
//                        "intent_recognize",           // 源节点：意图识别节点
//                        routeByIntent,                // 条件路由函数：返回 Command，Command.gotoNode() 对应 mappings 的 key
//                        Map.of(
//                                "query", "query_branch",      // 如果路由函数返回 "query"，则跳转到 query_branch
//                                "purchase", "purchase_branch", // 如果路由函数返回 "purchase"，则跳转到 purchase_branch
//                                "other", "other_branch"       // 如果路由函数返回 "other"，则跳转到 other_branch
//                        )
//                )
//
//                // 三个分支都连接到 END
//                .addEdge("query_branch", END)
//                .addEdge("purchase_branch", END)
//                .addEdge("other_branch", END)
//
//                // 编译图
//                .compile();
//    }
//
//    // ========== 5. 运行示例 ==========
//    public static void main(String[] args) throws GraphStateException {
//        var graph = buildGraph();
//
//        System.out.println("========== 示例 1: 查询意图 ==========");
//        var result1 = graph.stream(Map.of(
//                IntentState.INPUT_KEY, "我想查询商品信息"
//        )).stream()
//                .peek(output -> System.out.println("节点输出: " + output))
//                .reduce((a, b) -> b)
//                .map(NodeOutput::state);
//
//        result1.ifPresent(state -> {
//            System.out.println("最终结果: " + state.result().orElse("无结果"));
//            System.out.println("识别意图: " + state.intent().orElse("无意图"));
//        });
//
//        System.out.println("\n========== 示例 2: 购买意图 ==========");
//        var result2 = graph.stream(Map.of(
//                IntentState.INPUT_KEY, "我要购买这个商品"
//        )).stream()
//                .peek(output -> System.out.println("节点输出: " + output))
//                .reduce((a, b) -> b)
//                .map(NodeOutput::state);
//
//        result2.ifPresent(state -> {
//            System.out.println("最终结果: " + state.result().orElse("无结果"));
//            System.out.println("识别意图: " + state.intent().orElse("无意图"));
//        });
//
//        System.out.println("\n========== 示例 3: 其他意图 ==========");
//        var result3 = graph.stream(Map.of(
//                IntentState.INPUT_KEY, "你好，我想了解一下"
//        )).stream()
//                .peek(output -> System.out.println("节点输出: " + output))
//                .reduce((a, b) -> b)
//                .map(NodeOutput::state);
//
//        result3.ifPresent(state -> {
//            System.out.println("最终结果: " + state.result().orElse("无结果"));
//            System.out.println("识别意图: " + state.intent().orElse("无意图"));
//        });
//    }
//}
//
