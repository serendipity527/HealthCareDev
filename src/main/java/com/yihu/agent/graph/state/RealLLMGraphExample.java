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
 * 集成真实大模型API的示例
 * 
 * 使用说明：
 * 1. 需要添加langchain4j依赖（如果使用OpenAI）：
 *    <dependency>
 *        <groupId>dev.langchain4j</groupId>
 *        <artifactId>langchain4j-open-ai</artifactId>
 *        <version>0.29.1</version>
 *    </dependency>
 * 
 * 2. 设置环境变量 OPENAI_API_KEY（如果使用OpenAI）
 * 
 * 3. 或者使用其他大模型提供商（Claude、Ollama等）
 */
public class RealLLMGraphExample {

    /**
     * 定义状态类
     */
    static class ChatState extends AgentState {
        public static final Map<String, Channel<?>> SCHEMA = Map.of(
                "messages", Channels.appender(ArrayList::new),
                "user_input", Channels.base(() -> null),
                "model_response", Channels.base(() -> null)
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
     * 接收用户输入节点
     */
    static AsyncNodeAction<ChatState> receiveUserInputNode = node_async(state -> {
        System.out.println("[节点: receive_input] 接收用户输入");
        
        String userInput = state.userInput()
                .orElseThrow(() -> new RuntimeException("用户输入不能为空"));
        
        System.out.println("  用户输入: " + userInput);
        
        return Map.of(
                "messages", userInput,
                "user_input", null
        );
    });

    /**
     * 调用大模型节点（使用真实API）
     * 
     * 注意：这里需要根据你使用的大模型提供商来修改代码
     */
    static AsyncNodeAction<ChatState> callLLMNode = node_async(state -> {
        System.out.println("[节点: call_llm] 调用大模型");
        
        List<String> messages = state.messages();
        String lastUserMessage = messages.isEmpty() ? "" : messages.get(messages.size() - 1);
        
        // 调用真实的大模型API
        String modelResponse = callRealLLM(lastUserMessage);
        
        System.out.println("  模型回答: " + modelResponse);
        
        return Map.of(
                "messages", modelResponse,
                "model_response", modelResponse
        );
    });

    /**
     * 调用真实的大模型API
     * 
     * 示例1：使用OpenAI（需要langchain4j-open-ai依赖）
     */
    private static String callRealLLM(String userInput) {
        try {
            // 方式1：使用OpenAI（需要设置OPENAI_API_KEY环境变量）
            /*
            import dev.langchain4j.model.openai.OpenAiChatModel;
            
            ChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(System.getenv("OPENAI_API_KEY"))
                    .modelName("gpt-4o-mini")  // 或 "gpt-4", "gpt-3.5-turbo" 等
                    .temperature(0.7)
                    .maxTokens(1000)
                    .build();
            
            String response = chatModel.generate(userInput);
            return response;
            */

            // 方式2：使用Claude（需要langchain4j-anthropic依赖）
            /*
            import dev.langchain4j.model.anthropic.AnthropicChatModel;
            
            ChatModel chatModel = AnthropicChatModel.builder()
                    .apiKey(System.getenv("ANTHROPIC_API_KEY"))
                    .modelName("claude-3-sonnet-20240229")
                    .temperature(0.7)
                    .maxTokens(1000)
                    .build();
            
            String response = chatModel.generate(userInput);
            return response;
            */

            // 方式3：使用本地Ollama（需要langchain4j-ollama依赖）
            /*
            import dev.langchain4j.model.ollama.OllamaChatModel;
            
            ChatModel chatModel = OllamaChatModel.builder()
                    .baseUrl("http://localhost:11434")
                    .modelName("llama2")  // 或其他本地模型
                    .temperature(0.7)
                    .build();
            
            String response = chatModel.generate(userInput);
            return response;
            */

            // 方式4：使用Spring AI（如果项目使用Spring框架）
            /*
            import org.springframework.ai.chat.ChatClient;
            import org.springframework.ai.chat.prompt.Prompt;
            
            // 注入ChatClient bean
            ChatClient chatClient = ...; // 从Spring容器获取
            
            String response = chatClient.call(new Prompt(userInput))
                    .getResult()
                    .getOutput()
                    .getContent();
            return response;
            */

            // 临时模拟实现（实际使用时请替换为上面的真实API调用）
            return "这是对 '" + userInput + "' 的回答。请替换为真实的大模型API调用。";
            
        } catch (Exception e) {
            throw new RuntimeException("调用大模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 主方法：构建并运行graph
     */
    public static void main(String[] args) throws Exception {
        // 1. 创建StateGraph
        StateGraph<ChatState> graph = new StateGraph<>(ChatState.SCHEMA, ChatState::new)
                .addNode("receive_input", receiveUserInputNode)
                .addNode("call_llm", callLLMNode)
                .addEdge(START, "receive_input")
                .addEdge("receive_input", "call_llm")
                .addEdge("call_llm", END);

        // 2. 编译graph
        CompiledGraph<ChatState> compiledGraph = graph.compile();

        // 3. 准备输入
        Map<String, Object> inputs = Map.of(
                "user_input", "请用一句话介绍Java编程语言的特点"
        );

        // 4. 执行graph
        System.out.println("========== 开始执行Graph ==========\n");
        
        Optional<ChatState> finalState = compiledGraph.invoke(inputs);
        
        if (finalState.isPresent()) {
            ChatState state = finalState.get();
            System.out.println("\n========== 执行完成 ==========");
            System.out.println("最终对话历史: " + state.messages());
            System.out.println("模型回答: " + state.modelResponse().orElse("无"));
        }
    }
}

