package com.yihu.agent.controller;

import com.yihu.agent.dto.MessageDTO;
import com.yihu.agent.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket 消息控制器
 * 集成 LangChain4j 实现 AI 对话功能
 */
@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private AiChatService aiChatService;
    
    /**
     * 处理客户端发送的消息并使用 AI 生成回复
     * 
     * @param message 客户端发送的消息对象
     * @return 返回的消息对象
     */
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public MessageDTO handleMessage(MessageDTO message) {
        log.info("收到用户消息: {}", message.getContent());
        
        // 创建服务器回复消息
        MessageDTO response = new MessageDTO();
        response.setSender("小医AI助手");
        response.setTimestamp(LocalDateTime.now());
        
        try {
            // 使用 LangChain4j 生成 AI 回复
            String aiReply = aiChatService.chat(message.getContent());
            response.setContent(aiReply);
            log.info("AI 回复: {}", aiReply);
        } catch (Exception e) {
            log.error("AI 对话出错: {}", e.getMessage(), e);
            // 降级到简单回复
            response.setContent("抱歉，AI 服务暂时不可用。请稍后再试。😅\n" +
                    "错误信息: " + e.getMessage());
        }
        
        return response;
    }
}
