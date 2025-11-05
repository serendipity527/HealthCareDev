package com.yihu.agent.controller;

import com.yihu.agent.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Controller
public class WebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 点对点消息 - 使用 SimpMessagingTemplate 发送给指定用户
     * 
     * @param message 客户端发送的消息对象（需要包含目标用户信息）
     */
    @MessageMapping("/send/to/user")
    public void sendToSpecificUser(MessageDTO message) {
        log.info("Received message from {} to {}: {}", 
                message.getSender(), message.getReceiver(), message.getContent());
        
        message.setTimestamp(LocalDateTime.now());
        
        // 从消息对象中获取目标用户
        String targetUser = message.getReceiver();
        if (targetUser == null || targetUser.isEmpty()) {
            log.warn("Target user is null or empty, cannot send private message");
            return;
        }
        
        // 发送给指定用户，该用户需要订阅 /user/queue/messages
        messagingTemplate.convertAndSendToUser(
                targetUser, 
                "/queue/messages", 
                message
        );
        
        log.info("Sent private message from {} to {}", message.getSender(), targetUser);
    }
    
    /**
     * AI 对话消息处理
     * 接收用户消息，调用AI服务处理，并将AI回复发送回用户
     * 
     * @param message 客户端发送的消息对象
     */
    @MessageMapping("/chat/ai")
    public void chatWithAI(MessageDTO message) {
        log.info("Received AI chat message from {}: {}", message.getSender(), message.getContent());
        
        message.setTimestamp(LocalDateTime.now());
        String username = message.getSender();
        
        try {
            // TODO: 在这里调用您的AI服务
            // 示例：String aiResponse = aiService.chat(message.getContent());
            
            // 模拟AI处理延迟（实际使用时删除此行）
            Thread.sleep(1000);
            
            // 临时演示：简单回复（实际使用时替换为真实的AI响应）
            String aiResponse = generateMockAIResponse(message.getContent());
            
            // 构建AI回复消息
            MessageDTO aiReply = new MessageDTO();
            aiReply.setContent(aiResponse);
            aiReply.setSender("AI");
            aiReply.setReceiver(username);
            aiReply.setType(MessageDTO.MessageType.CHAT);
            aiReply.setTimestamp(LocalDateTime.now());
            
            // 发送AI回复给用户（点对点）
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/ai-reply",
                    aiReply
            );
            
            log.info("Sent AI reply to user: {}", username);
            
        } catch (Exception e) {
            log.error("Error processing AI chat for user {}: {}", username, e.getMessage(), e);
            
            // 发送错误消息
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setContent("抱歉，AI 服务暂时不可用，请稍后再试。");
            errorMessage.setSender("AI");
            errorMessage.setReceiver(username);
            errorMessage.setType(MessageDTO.MessageType.SYSTEM);
            errorMessage.setTimestamp(LocalDateTime.now());
            
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/ai-reply",
                    errorMessage
            );
        }
    }
    
    /**
     * 临时方法：生成模拟的AI回复
     * TODO: 替换为真实的AI服务调用
     * 
     * @param userMessage 用户消息
     * @return AI回复
     */
    private String generateMockAIResponse(String userMessage) {
        // 这里是临时的模拟回复，您可以根据需要替换为真实的AI服务
        if (userMessage.contains("你好") || userMessage.contains("您好")) {
            return "你好！我是AI智能助手，很高兴为您服务。有什么我可以帮助您的吗？";
        } else if (userMessage.contains("天气")) {
            return "很抱歉，我目前还无法查询实时天气信息。不过您可以告诉我您所在的城市，我可以为您提供一些天气相关的建议。";
        } else if (userMessage.contains("帮助") || userMessage.contains("功能")) {
            return "我是一个智能助手，可以回答您的问题、提供建议和进行对话。请告诉我您需要什么帮助！";
        } else {
            return "感谢您的消息：\"" + userMessage + "\"。我已经收到了！目前我还在学习中，后续会接入更强大的AI能力来更好地回答您的问题。";
        }
    }
}
