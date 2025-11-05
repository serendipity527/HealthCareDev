package com.yihu.agent.graph.enums;

/**
 * 风险等级枚举
 */
public enum RiskLevel {
    /**
     * 高危 - 需要立即就医
     */
    HIGH,
    
    /**
     * 中危 - 需要密切关注
     */
    MEDIUM,
    
    /**
     * 低危 - 可以给予建议
     */
    LOW,
    
    /**
     * 未评估
     */
    UNASSESSED
}

