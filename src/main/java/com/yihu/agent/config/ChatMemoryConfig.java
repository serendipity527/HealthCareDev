package com.yihu.agent.config;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天记忆配置类
 * 配置 AI 对话的记忆功能，实现多轮对话
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 创建聊天记忆 Bean
     * 使用 MessageWindowChatMemory 保留最近的对话历史
     * 
     * @return ChatMemory 实例
     */
    @Bean
    public ChatMemory chatMemory() {
        // 创建一个消息窗口记忆，保留最近 10 条消息（5轮对话）
        // 可以根据需要调整 maxMessages 参数
        return MessageWindowChatMemory.withMaxMessages(10);
    }
}



