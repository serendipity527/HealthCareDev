package com.yihu.agent.graph.state;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 一个简单的示例，展示如何创建一个graph来：
 * 1. 接受用户输入
 * 2. 调用大模型生成回答
 * 3. 返回结果给用户
 */
public class SimpleLLMGraphExample {

    /**
     * 定义状态类，包含用户输入和模型输出
     */
    static class ChatState extends AgentState {
        // 定义状态schema：用户输入和模型回答都存储在messages列表中
        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                "messages", Channels.appender(ArrayList::new),  // messages用于存储对话历史
                "user_input", Channels.base(() -> null),      // user_input存储当前用户输入
                "model_response", Channels.base(() -> null)    // model_response存储模型回答
        );

        public ChatState(Map<String, Object> initData) {
            super(initData);
        }

        public List<String> messages() {
            return this.<List<String>>value("messages")
                    .orElse(new ArrayList<>());
        }

        public Optional<String> userInput() {
            return this.<String>value("user_input");
        }

        public Optional<String> modelResponse() {
            return this.<String>value("model_response");
        }
    }

    /**
     * 节点1：接收用户输入节点
     * 这个节点从状态中获取用户输入，并将其添加到messages中
     */
    static AsyncNodeAction<ChatState> receiveUserInputNode = node_async(state -> {
        System.out.println("=== 接收用户输入节点 ===");
        
        // 从状态中获取用户输入
        String userInput = state.userInput()
                .orElseThrow(() -> new RuntimeException("用户输入不能为空"));
        
        System.out.println("用户输入: " + userInput);
        
        // 将用户输入添加到messages中
        return Map.of(
                "messages", userInput,
                "user_input", null  // 清空user_input，避免重复处理
        );
    });

    /**
     * 节点2：调用大模型生成回答
     * 在实际应用中，这里应该调用真实的大模型API（如OpenAI、Claude等）
     */
    static AsyncNodeAction<ChatState> callLLMNode = node_async(state -> {
        System.out.println("=== 调用大模型节点 ===");
        
        // 获取对话历史
        List<String> messages = state.messages();
        System.out.println("当前对话历史: " + messages);
        
        // 获取最后一条用户消息
        String lastUserMessage = messages.isEmpty() ? "" : messages.get(messages.size() - 1);
        
        // 模拟调用大模型（在实际应用中，这里应该调用真实的大模型API）
        String modelResponse = callLLM(lastUserMessage);
        
        System.out.println("模型回答: " + modelResponse);
        
        // 将模型回答添加到messages中，并更新model_response字段
        return Map.of(
                "messages", modelResponse,
                "model_response", modelResponse
        );
    });

    /**
     * 模拟大模型调用
     * 在实际应用中，这里应该替换为真实的大模型API调用
     * 例如：
     * - OpenAI: 使用 OpenAiChatModel
     * - Claude: 使用 AnthropicChatModel
     * - 本地模型: 使用 OllamaChatModel
     */
    private static String callLLM(String userInput) {
        // 这里是模拟实现，实际应该调用真实的大模型API
        // 示例：使用 langchain4j 调用 OpenAI
        /*
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4")
                .temperature(0.7)
                .build();
        
        String response = chatModel.generate(userInput);
        return response;
        */
        
        // 模拟返回
        return "这是对 '" + userInput + "' 的回答。在实际应用中，这里会调用真实的大模型API。";
    }

    /**
     * 构建并运行graph
     */
    public static void main(String[] args) throws Exception {
        // 1. 创建StateGraph
        StateGraph<ChatState> graph = new StateGraph<>(ChatState.SCHEMA, ChatState::new)
                // 添加节点
                .addNode("receive_input", receiveUserInputNode)  // 接收用户输入节点
                .addNode("call_llm", callLLMNode)                // 调用大模型节点
                // 添加边，定义节点之间的连接关系
                .addEdge(START, "receive_input")                 // 从START开始，先执行接收用户输入
                .addEdge("receive_input", "call_llm")           // 接收输入后，调用大模型
                .addEdge("call_llm", END);                        // 大模型调用完成后，结束

        // 2. 编译graph
        CompiledGraph<ChatState> compiledGraph = graph.compile();

        // 3. 准备输入数据（用户输入）
        Map<String, Object> inputs = Map.of(
                "user_input", "你好，请介绍一下Java编程语言"
        );

        // 4. 运行graph并获取结果
        System.out.println("\n========== 开始执行Graph ==========\n");
        
        Optional<ChatState> finalState = compiledGraph.invoke(inputs);
        
        if (finalState.isPresent()) {
            ChatState state = finalState.get();
            System.out.println("\n========== 执行完成 ==========");
            System.out.println("最终状态:");
            System.out.println("  Messages: " + state.messages());
            System.out.println("  模型回答: " + state.modelResponse().orElse("无"));
        } else {
            System.out.println("Graph执行失败，未返回最终状态");
        }

        // 5. 也可以使用stream方式逐步查看执行过程
        System.out.println("\n========== 使用Stream方式执行 ==========\n");
        for (var output : compiledGraph.stream(inputs)) {
            System.out.println("节点: " + output.node());
            System.out.println("状态: " + output.state().data());
            System.out.println("---");
        }
    }
}

