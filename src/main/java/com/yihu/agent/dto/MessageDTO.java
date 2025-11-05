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
     * 发送者
     */
    private String sender;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 发送时间
     */
    private LocalDateTime timestamp;
}

