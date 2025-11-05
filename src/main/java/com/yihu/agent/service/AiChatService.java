package com.yihu.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * AI 聊天服务接口
 * 使用 LangChain4j 的 @AiService 注解自动实现
 */
@AiService
public interface AiChatService {

    /**
     * 与 AI 进行对话
     * 
     * @param userMessage 用户消息
     * @return AI 回复
     */
    @SystemMessage("""
            你是一个友好、专业的AI助手，名字叫"小医"。
            你的任务是帮助用户解答问题、提供建议和进行愉快的对话。
            
            请遵循以下原则：
            1. 保持友好、礼貌和专业的态度
            2. 提供准确、有帮助的信息
            3. 如果不确定答案，诚实地告诉用户
            4. 回答要简洁明了，避免过于冗长
            5. 适当使用emoji让对话更生动（但不要过度使用）
            6. 对于医疗健康相关的问题，提供一般性建议但提醒用户咨询专业医生
            
            现在，请开始与用户对话吧！
            """)
    String chat(String userMessage);
}

