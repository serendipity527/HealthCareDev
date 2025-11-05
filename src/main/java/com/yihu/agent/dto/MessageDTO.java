package com.yihu.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket 消息传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 发送者
     */
    private String sender;
    
    /**
     * 接收者（用于点对点通信）
     */
    private String receiver;
    
    /**
     * 消息类型（如：CHAT, SYSTEM, JOIN, LEAVE等）
     */
    private MessageType type;
    
    /**
     * 发送时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /**
         * 聊天消息
         */
        CHAT,
        
        /**
         * 系统消息
         */
        SYSTEM,
        
        /**
         * 用户加入
         */
        JOIN,
        
        /**
         * 用户离开
         */
        LEAVE
    }
}

