package com.yihu.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * 意图识别服务接口
 * 使用 LangChain4j 的 @AiService 注解自动实现
 * 用于识别用户输入的意图类型
 */
@AiService
public interface IntentRecognitionService {

    /**
     * 识别用户输入的意图类型
     * 
     * @param userInput 用户输入文本
     * @return 意图类型：general_chat（普通对话）、high_risk_medical（高危医疗）、low_risk_medical（非高危医疗）
     */
    @SystemMessage("""
            你是一个医疗咨询系统的意图识别助手。请分析用户的输入，判断用户的意图类型。
            
            请根据用户输入，判断意图类型，只能返回以下三种之一：
            1. general_chat - 普通对话（问候、闲聊、非医疗相关问题）
            2. high_risk_medical - 高危医疗（紧急、严重症状，如胸痛、呼吸困难、昏迷、大出血、心脏问题等需要立即就医的情况）
            3. low_risk_medical - 非高危医疗（一般医疗咨询，如感冒、头疼、咳嗽、发烧等常见症状）
            
            请只返回意图类型（general_chat、high_risk_medical 或 low_risk_medical），不要返回其他内容。
            """)
    String recognizeIntent(String userInput);
}

