//package com.yihu.agent.demos.ConditionalEdgeDemo;
//
//import org.bsc.langgraph4j.*;
//import org.bsc.langgraph4j.action.NodeAction;
//import org.bsc.langgraph4j.state.AgentState;
//import org.bsc.langgraph4j.state.Channel;
//
//import java.util.Map;
//import java.util.Optional;
//
//import static org.bsc.langgraph4j.StateGraph.END;
//import static org.bsc.langgraph4j.StateGraph.START;
//import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
//import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
//
///**
// * 基础意图识别示例
// *
// * 这个示例展示了如何使用简单的规则或逻辑来实现意图识别，
// * 并根据识别出的意图路由到不同的子图进行处理。
// *
// * 适用场景：
// * - 意图类型明确且有限
// * - 可以通过关键词或简单规则识别
// * - 不需要复杂的自然语言理解
// */
//public class BasicIntentRecognitionExample {
//
//    /**
//     * 状态类：扩展 AgentState，包含意图信息
//     */
//    static class State extends AgentState {
//        public static Map<String, Channel<?>> SCHEMA = Map.of();
//
//        public State(Map<String, Object> initData) {
//            super(initData);
//        }
//
//        /**
//         * 获取当前识别的意图
//         * @return 意图字符串（如 "explain", "query" 等）
//         */
//        public Optional<String> intent() {
//            return value("intent");
//        }
//
//        /**
//         * 获取用户输入
//         * @return 用户输入的文本
//         */
//        public Optional<String> input() {
//            return value("input");
//        }
//    }
//
//    /**
//     * 意图识别节点
//     *
//     * 这个节点负责分析用户输入并识别意图。
//     * 在实际应用中，你可以：
//     * 1. 使用关键词匹配
//     * 2. 使用正则表达式
//     * 3. 使用简单的NLP库
//     * 4. 调用外部API
//     */
//    static class IntentRecognizeNode implements NodeAction<State> {
//
//        /**
//         * 识别用户输入的意图
//         *
//         * @param state 当前状态，包含用户输入等信息
//         * @return 包含识别出的意图的Map
//         */
//        @Override
//        public Map<String, Object> apply(State state) {
//            // 获取用户输入
//            String input = state.input()
//                    .orElseThrow(() -> new IllegalStateException("用户输入不能为空"));
//
//            // 简单的意图识别逻辑（实际应用中可以使用更复杂的规则）
//            String intent = recognizeIntent(input);
//
//            System.out.println("用户输入: " + input);
//            System.out.println("识别出的意图: " + intent);
//
//            // 将识别出的意图存储到状态中
//            return Map.of("intent", intent);
//        }
//
//        /**
//         * 简单的意图识别逻辑
//         * 实际应用中可以使用更复杂的规则或算法
//         */
//        private String recognizeIntent(String input) {
//            String lowerInput = input.toLowerCase();
//
//            // 根据关键词识别意图
//            if (lowerInput.contains("解释") || lowerInput.contains("说明") ||
//                lowerInput.contains("explain") || lowerInput.contains("what is")) {
//                return "explain";
//            } else if (lowerInput.contains("查询") || lowerInput.contains("搜索") ||
//                       lowerInput.contains("query") || lowerInput.contains("search") ||
//                       lowerInput.contains("find")) {
//                return "query";
//            } else if (lowerInput.contains("计算") || lowerInput.contains("算") ||
//                       lowerInput.contains("calculate") || lowerInput.contains("compute")) {
//                return "calculate";
//            } else {
//                // 默认意图
//                return "general";
//            }
//        }
//    }
//
//    /**
//     * 解释子图：处理"explain"意图
//     */
//    private static StateGraph<State> createExplainSubGraph() throws GraphStateException {
//        return new StateGraph<>(State::new)
//                .addNode("explain_work", node_async(state -> {
//                    System.out.println("执行解释任务...");
//                    String input = state.input().orElse("");
//                    return Map.of("result", "这是关于 '" + input + "' 的详细解释");
//                }))
//                .addEdge(START, "explain_work")
//                .addEdge("explain_work", END);
//    }
//
//    /**
//     * 查询子图：处理"query"意图
//     */
//    private static StateGraph<State> createQuerySubGraph() throws GraphStateException {
//        return new StateGraph<>(State::new)
//                .addNode("query_work", node_async(state -> {
//                    System.out.println("执行查询任务...");
//                    String input = state.input().orElse("");
//                    return Map.of("result", "查询结果: 找到了关于 '" + input + "' 的相关信息");
//                }))
//                .addEdge(START, "query_work")
//                .addEdge("query_work", END);
//    }
//
//    /**
//     * 计算子图：处理"calculate"意图
//     */
//    private static StateGraph<State> createCalculateSubGraph() throws GraphStateException {
//        return new StateGraph<>(State::new)
//                .addNode("calculate_work", node_async(state -> {
//                    System.out.println("执行计算任务...");
//                    String input = state.input().orElse("");
//                    return Map.of("result", "计算结果: " + input);
//                }))
//                .addEdge(START, "calculate_work")
//                .addEdge("calculate_work", END);
//    }
//
//    /**
//     * 通用处理子图：处理其他意图
//     */
//    private static StateGraph<State> createGeneralSubGraph() throws GraphStateException {
//        return new StateGraph<>(State::new)
//                .addNode("general_work", node_async(state -> {
//                    System.out.println("执行通用处理任务...");
//                    String input = state.input().orElse("");
//                    return Map.of("result", "通用处理结果: " + input);
//                }))
//                .addEdge(START, "general_work")
//                .addEdge("general_work", END);
//    }
//
//    /**
//     * 构建主图：包含意图识别节点和多个子图
//     */
//    public static StateGraph<State> buildGraph() throws GraphStateException {
//        // 创建子图
//        var explainSubGraph = createExplainSubGraph();
//        var querySubGraph = createQuerySubGraph();
//        var calculateSubGraph = createCalculateSubGraph();
//        var generalSubGraph = createGeneralSubGraph();
//
//        // 创建意图识别节点
//        var intentRecognizeNode = new IntentRecognizeNode();
//
//        // 构建主图
//        return new StateGraph<>(State::new)
//                // 添加意图识别节点
//                .addNode("intent_recognize", node_async(intentRecognizeNode))
//                // 添加子图作为节点
//                .addNode("explain_agent", explainSubGraph)
//                .addNode("query_agent", querySubGraph)
//                .addNode("calculate_agent", calculateSubGraph)
//                .addNode("general_agent", generalSubGraph)
//                // 从起始节点到意图识别节点
//                .addEdge(START, "intent_recognize")
//                // 根据意图路由到不同的子图
//                .addConditionalEdges("intent_recognize",
//                        edge_async(state -> state.intent().orElseThrow()),
//                        Map.of(
//                                "explain", "explain_agent",
//                                "query", "query_agent",
//                                "calculate", "calculate_agent",
//                                "general", "general_agent"
//                        ))
//                // 所有子图完成后都结束
//                .addEdge("explain_agent", END)
//                .addEdge("query_agent", END)
//                .addEdge("calculate_agent", END)
//                .addEdge("general_agent", END);
//    }
//
//    /**
//     * 运行示例
//     */
//    public static void main(String[] args) throws GraphStateException {
//        System.out.println("=== 基础意图识别示例 ===\n");
//
//        // 构建并编译图
//        var graph = buildGraph();
//        var workflow = graph.compile();
//
//        // 测试不同的意图
//        String[] testInputs = {
//                "请解释一下什么是人工智能",
//                "查询今天的天气",
//                "计算 1 + 1 等于多少",
//                "你好，我想了解一下"
//        };
//
//        for (String input : testInputs) {
//            System.out.println("\n" + "=".repeat(50));
//            System.out.println("处理输入: " + input);
//            System.out.println("=".repeat(50));
//
//            // 执行工作流
//            var result = workflow.stream(Map.of("input", input))
//                    .reduce((a, b) -> b)
//                    .map(NodeOutput::state);
//
//            if (result.isPresent()) {
//                var finalState = result.get();
//                System.out.println("\n最终结果: " + finalState.data().get("result"));
//                System.out.println("识别的意图: " + finalState.intent().orElse("未知"));
//            }
//        }
//    }
//}
//
