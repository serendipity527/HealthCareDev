package com.yihu.agent.graph.enums;

/**
 * 意图类型枚举
 */
public enum IntentType {
    /**
     * 高危医疗 - 需要立即医疗干预
     */
    EMERGENCY_MEDICAL,
    
    /**
     * 通用聊天 - 非医疗相关的日常对话
     */
    GENERAL_CHAT,
    
    /**
     * 医疗意图 - 非明确高危的医疗咨询
     */
    MEDICAL_INQUIRY,
    
    /**
     * 未知 - 需要进一步判断
     */
    UNKNOWN
}

