package com.yihu.agent.controller;

import com.yihu.agent.dto.MessageDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket 消息控制器
 */
@Controller
public class WebSocketController {
    
    /**
     * 处理客户端发送的消息并广播给所有订阅者
     * 
     * @param message 客户端发送的消息对象
     * @return 返回的消息对象
     */
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public MessageDTO handleMessage(MessageDTO message) {
        // 创建服务器回复消息
        MessageDTO response = new MessageDTO();
        response.setSender("AI助手");
        response.setTimestamp(LocalDateTime.now());
        
        // 根据用户消息内容生成回复
        String userMessage = message.getContent().toLowerCase();
        String reply = "你好！👋 很高兴见到你，有什么可以帮助你的吗？";
        
        if (userMessage.contains("你好") || userMessage.contains("hello") || userMessage.contains("hi")) {
            reply = "你好！👋 很高兴见到你，有什么可以帮助你的吗？";
        } else if (userMessage.contains("时间")) {
            reply = "现在的时间是：" + LocalDateTime.now().toString().substring(0, 19).replace("T", " ");
        } else if (userMessage.contains("天气")) {
            reply = "抱歉，我目前还不能查询天气信息。但我可以和你聊天！😊";
        } else if (userMessage.contains("谢谢") || userMessage.contains("感谢")) {
            reply = "不客气！😊 很高兴能帮到你！";
        } else if (userMessage.contains("再见") || userMessage.contains("拜拜")) {
            reply = "再见！👋 期待下次与你交流！";
        } else if (userMessage.contains("你是谁") || userMessage.contains("介绍")) {
            reply = "我是一个基于WebSocket的AI助手，可以实时与你进行对话交流。🤖";
        } else if (userMessage.contains("功能") || userMessage.contains("能做什么")) {
            reply = "我可以和你聊天、回答问题，还在不断学习中！💪 试试问我一些问题吧！";
        } else if (userMessage.contains("帮助") || userMessage.contains("help")) {
            reply = "你可以问我：\n• 打招呼（你好、hello）\n• 询问时间\n• 聊天交流\n• 或者随便说些什么 😊";
        } else {
            // 默认回复
            String[] defaultReplies = {
                "我明白了，" + message.getContent() + " 🤔",
                "有趣的观点！能详细说说吗？",
                "嗯，关于「" + message.getContent() + "」，我觉得这是个不错的话题！",
                "收到你的消息了：" + message.getContent() + " 👍",
                "让我想想... 关于这个问题，你觉得呢？"
            };
            reply = defaultReplies[(int) (Math.random() * defaultReplies.length)];
        }
        
        response.setContent(reply);
        return response;
    }
}
