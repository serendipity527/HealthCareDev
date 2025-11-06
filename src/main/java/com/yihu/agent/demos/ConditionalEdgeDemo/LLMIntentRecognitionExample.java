//package com.yihu.agent.demos.ConditionalEdgeDemo;
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.ChatMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatModel;
//import dev.langchain4j.model.output.structured.Description;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.service.SystemMessage;
//import dev.langchain4j.service.V;
//import org.bsc.langgraph4j.*;
//import org.bsc.langgraph4j.action.NodeAction;
//import org.bsc.langgraph4j.langchain4j.serializer.std.ChatMesssageSerializer;
//import org.bsc.langgraph4j.langchain4j.serializer.std.ToolExecutionRequestSerializer;
//import org.bsc.langgraph4j.prebuilt.MessagesState;
//import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
//
//import java.util.Map;
//import java.util.Optional;
//
//import static java.lang.String.format;
//import static org.bsc.langgraph4j.StateGraph.END;
//import static org.bsc.langgraph4j.StateGraph.START;
//import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
//import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
//
///**
// * 使用LLM进行意图识别示例
// *
// * 这个示例展示了如何使用大语言模型（LLM）来理解用户意图，
// * 并根据识别出的意图智能路由到不同的处理节点。
// *
// * 适用场景：
// * - 需要理解复杂的自然语言
// * - 意图类型多样且可能重叠
// * - 需要上下文理解
// * - 意图识别准确度要求高
// *
// * 注意：运行此示例需要配置LLM模型（如OpenAI、Ollama等）
// */
//public class LLMIntentRecognitionExample {
//
//    /**
//     * 状态类：扩展 MessagesState，包含消息和路由信息
//     */
//    static class State extends MessagesState<ChatMessage> {
//
//        public State(Map<String, Object> initData) {
//            super(initData);
//        }
//
//        /**
//         * 获取下一个要路由到的节点名称
//         * @return 下一个节点的名称
//         */
//        public Optional<String> next() {
//            return this.value("next");
//        }
//    }
//
//    /**
//     * 状态序列化器：支持消息和工具执行请求的序列化
//     */
//    static class StateSerializer extends ObjectStreamStateSerializer<State> {
//
//        public StateSerializer() {
//            super(State::new);
//            mapper().register(dev.langchain4j.agent.tool.ToolExecutionRequest.class,
//                    new ToolExecutionRequestSerializer());
//            mapper().register(ChatMessage.class, new ChatMesssageSerializer());
//        }
//    }
//
//    /**
//     * 监督者代理（Supervisor Agent）
//     *
//     * 使用LLM来理解用户意图并决定路由到哪个工作节点
//     */
//    static class SupervisorAgent implements NodeAction<State> {
//
//        /**
//         * 路由结果类：包含下一个要路由到的节点名称
//         */
//        static class Router {
//            @Description("Worker to route to next. If no workers needed, route to FINISH.")
//            String next;
//
//            @Override
//            public String toString() {
//                return format("Router[next: %s]", next);
//            }
//        }
//
//        /**
//         * LLM服务接口：用于意图识别和路由决策
//         */
//        interface Service {
//            @SystemMessage("""
//                    You are a supervisor tasked with managing a conversation between the following workers: {{members}}.
//                    Given the following user request, respond with the worker to act next.
//                    Each worker will perform a task and respond with their results and status.
//                    When finished, respond with FINISH.
//
//                    Available workers:
//                    - researcher: Use this for research tasks, information queries, fact-finding
//                    - coder: Use this for coding tasks, calculations, programming questions
//                    - writer: Use this for writing tasks, content generation, text creation
//                    - FINISH: Use this when the task is complete or no worker is needed
//                    """)
//            Router evaluate(@V("members") String members,
//                           @dev.langchain4j.service.UserMessage String userMessage);
//        }
//
//        final Service service;
//        public final String[] members = {"researcher", "coder", "writer"};
//
//        /**
//         * 构造函数：初始化LLM服务
//         *
//         * @param model ChatModel实例，可以是OpenAI、Ollama等
//         */
//        public SupervisorAgent(ChatModel model) {
//            service = AiServices.create(Service.class, model);
//        }
//
//        @Override
//        public Map<String, Object> apply(State state) throws Exception {
//            // 获取最后一条消息
//            var message = state.lastMessage()
//                    .orElseThrow(() -> new IllegalStateException("消息不能为空"));
//
//            // 提取消息文本
//            var text = switch (message.type()) {
//                case USER -> ((UserMessage) message).singleText();
//                case AI -> ((AiMessage) message).text();
//                default -> throw new IllegalStateException("意外的消息类型: " + message.type());
//            };
//
//            System.out.println("监督者分析用户输入: " + text);
//
//            // 使用LLM进行意图识别和路由决策
//            var membersStr = String.join(",", members);
//            var result = service.evaluate(membersStr, text);
//
//            System.out.println("监督者决策: 路由到 -> " + result.next);
//
//            // 将路由决策存储到状态中
//            return Map.of("next", result.next);
//        }
//    }
//
//    /**
//     * 研究代理（Researcher Agent）
//     * 处理研究、查询、信息检索等任务
//     */
//    static class ResearchAgent implements NodeAction<State> {
//
//        @Override
//        public Map<String, Object> apply(State state) throws Exception {
//            var message = state.lastMessage()
//                    .orElseThrow(() -> new IllegalStateException("消息不能为空"));
//
//            var text = switch (message.type()) {
//                case USER -> ((UserMessage) message).singleText();
//                case AI -> ((AiMessage) message).text();
//                default -> throw new IllegalStateException("意外的消息类型: " + message.type());
//            };
//
//            System.out.println("研究代理处理: " + text);
//
//            // 模拟研究任务（实际应用中可以调用搜索API、数据库等）
//            String result = "根据研究，关于 '" + text + "' 的信息如下：\n" +
//                    "这是一个需要深入研究的话题，涉及多个方面的知识。";
//
//            return Map.of("messages", AiMessage.from(result));
//        }
//    }
//
//    /**
//     * 编程代理（Coder Agent）
//     * 处理编程、计算、代码相关任务
//     */
//    static class CoderAgent implements NodeAction<State> {
//
//        @Override
//        public Map<String, Object> apply(State state) throws Exception {
//            var message = state.lastMessage()
//                    .orElseThrow(() -> new IllegalStateException("消息不能为空"));
//
//            var text = switch (message.type()) {
//                case USER -> ((UserMessage) message).singleText();
//                case AI -> ((AiMessage) message).text();
//                default -> throw new IllegalStateException("意外的消息类型: " + message.type());
//            };
//
//            System.out.println("编程代理处理: " + text);
//
//            // 模拟编程/计算任务
//            String result = "代码执行结果：\n" +
//                    "已处理请求: " + text + "\n" +
//                    "计算完成，结果已生成。";
//
//            return Map.of("messages", AiMessage.from(result));
//        }
//    }
//
//    /**
//     * 写作代理（Writer Agent）
//     * 处理写作、内容生成、文本创作等任务
//     */
//    static class WriterAgent implements NodeAction<State> {
//
//        @Override
//        public Map<String, Object> apply(State state) throws Exception {
//            var message = state.lastMessage()
//                    .orElseThrow(() -> new IllegalStateException("消息不能为空"));
//
//            var text = switch (message.type()) {
//                case USER -> ((UserMessage) message).singleText();
//                case AI -> ((AiMessage) message).text();
//                default -> throw new IllegalStateException("意外的消息类型: " + message.type());
//            };
//
//            System.out.println("写作代理处理: " + text);
//
//            // 模拟写作任务
//            String result = "生成的内容：\n" +
//                    "基于您的要求 '" + text + "'，我已经为您创建了相应的内容。\n" +
//                    "内容已准备就绪，可以查看。";
//
//            return Map.of("messages", AiMessage.from(result));
//        }
//    }
//
//    /**
//     * 构建主图：包含监督者节点和多个工作节点
//     */
//    public static StateGraph<State> buildGraph(ChatModel supervisorModel) throws GraphStateException {
//        // 创建工作代理
//        var supervisor = new SupervisorAgent(supervisorModel);
//        var researcher = new ResearchAgent();
//        var coder = new CoderAgent();
//        var writer = new WriterAgent();
//
//        // 构建状态图
//        return new StateGraph<>(State.SCHEMA, new StateSerializer())
//                // 添加监督者节点
//                .addNode("supervisor", node_async(supervisor))
//                // 添加工作节点
//                .addNode("researcher", node_async(researcher))
//                .addNode("coder", node_async(coder))
//                .addNode("writer", node_async(writer))
//                // 从起始节点到监督者
//                .addEdge(START, "supervisor")
//                // 根据监督者的决策路由到不同的工作节点
//                .addConditionalEdges("supervisor",
//                        edge_async(state -> state.next().orElseThrow()),
//                        Map.of(
//                                "FINISH", END,
//                                "researcher", "researcher",
//                                "coder", "coder",
//                                "writer", "writer"
//                        ))
//                // 工作节点完成后返回监督者（可以继续处理或结束）
//                .addEdge("researcher", "supervisor")
//                .addEdge("coder", "supervisor")
//                .addEdge("writer", "supervisor");
//    }
//
//    /**
//     * 运行示例
//     *
//     * 注意：需要配置LLM模型才能运行
//     * 可以使用以下方式之一：
//     * 1. OpenAI: OpenAiChatModel.builder().apiKey("your-key").build()
//     * 2. Ollama: OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName("llama3").build()
//     */
//    public static void main(String[] args) throws GraphStateException {
//        System.out.println("=== 使用LLM进行意图识别示例 ===\n");
//
//        // 注意：这里需要配置实际的LLM模型
//        // 示例使用Ollama（需要本地运行Ollama服务）
//        /*
//        ChatModel model = dev.langchain4j.model.ollama.OllamaChatModel.builder()
//                .baseUrl("http://localhost:11434")
//                .modelName("llama3")
//                .temperature(0.0)
//                .build();
//        */
//
//        // 如果没有配置LLM，显示提示信息
//        System.out.println("注意：此示例需要配置LLM模型才能运行。");
//        System.out.println("请取消注释main方法中的模型配置代码，或使用其他LLM提供商。\n");
//
//        // 示例：如何配置和使用
//        /*
//        ChatModel model = ...; // 配置你的LLM模型
//
//        var graph = buildGraph(model);
//        var workflow = graph.compile();
//
//        // 测试不同的输入
//        String[] testInputs = {
//                "请帮我研究一下人工智能的发展历史",
//                "写一个计算斐波那契数列的Java程序",
//                "帮我写一篇关于春天的文章",
//                "查询一下今天的天气情况"
//        };
//
//        for (String input : testInputs) {
//            System.out.println("\n" + "=".repeat(50));
//            System.out.println("处理输入: " + input);
//            System.out.println("=".repeat(50));
//
//            var result = workflow.stream(Map.of("messages", UserMessage.from(input)))
//                    .reduce((a, b) -> b)
//                    .map(NodeOutput::state);
//
//            if (result.isPresent()) {
//                var finalState = result.get();
//                var lastMessage = finalState.lastMessage();
//                if (lastMessage.isPresent() && lastMessage.get() instanceof AiMessage) {
//                    System.out.println("\n最终回复: " + ((AiMessage) lastMessage.get()).text());
//                }
//            }
//        }
//        */
//    }
//
//    /**
//     * 使用示例：展示如何在实际项目中使用
//     */
//    public static void runExample(ChatModel model, String userInput) throws GraphStateException {
//        var graph = buildGraph(model);
//        var workflow = graph.compile();
//
//        System.out.println("\n" + "=".repeat(50));
//        System.out.println("处理输入: " + userInput);
//        System.out.println("=".repeat(50));
//
//        var result = workflow.stream(Map.of("messages", UserMessage.from(userInput)))
//                .reduce((a, b) -> b)
//                .map(NodeOutput::state);
//
//        if (result.isPresent()) {
//            var finalState = result.get();
//            var lastMessage = finalState.lastMessage();
//            if (lastMessage.isPresent() && lastMessage.get() instanceof AiMessage) {
//                System.out.println("\n最终回复: " + ((AiMessage) lastMessage.get()).text());
//            }
//        }
//    }
//}
//
