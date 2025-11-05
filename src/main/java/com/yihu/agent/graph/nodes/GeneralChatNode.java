package com.yihu.agent.graph.nodes;

import com.yihu.agent.graph.state.MedicalConsultationState;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * GeneralChat节点 - 处理通用聊天对话
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralChatNode {
    
    private final OpenAiChatModel chatLanguageModel;
    
    /**
     * 处理通用聊天
     */
    public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
        log.info("GeneralChatNode: 处理通用聊天");
        
        // 构建上下文
        StringBuilder context = new StringBuilder();
        if (!state.getConversationHistory().isEmpty()) {
            context.append("对话历史：\n");
            // 只保留最近的几条历史记录
            int start = Math.max(0, state.getConversationHistory().size() - 5);
            for (int i = start; i < state.getConversationHistory().size(); i++) {
                context.append(state.getConversationHistory().get(i)).append("\n");
            }
        }
        
        // 生成回复
        String prompt = String.format("""
                你是一个友好、专业的AI助手，名字叫"小医"。
                用户正在和你进行日常对话。请提供友好、有帮助的回复。
                
                %s
                
                当前用户输入：%s
                
                请生成一个简洁、友好的回复：
                """, context, state.getUserInput());
        
        String response = chatLanguageModel.chat(prompt);
        
        state.setResponse(response);
        state.addToHistory("小医", response);
        
        log.info("GeneralChatNode: 通用回复已生成");
        
        return CompletableFuture.completedFuture(state.data());
    }
}

