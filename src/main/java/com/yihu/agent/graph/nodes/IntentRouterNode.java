package com.yihu.agent.graph.nodes;

import com.yihu.agent.graph.enums.IntentType;
import com.yihu.agent.graph.enums.RiskLevel;
import com.yihu.agent.graph.state.MedicalConsultationState;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * IntentRouter节点 - 意图路由，判断用户意图类型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntentRouterNode {
    
    private final OpenAiChatModel chatLanguageModel;
    
    /**
     * 路由用户意图
     */
    public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
        log.info("IntentRouterNode: 开始意图识别");
        
        String userInput = state.getUserInput();
        
        // 使用LLM进行意图识别
        String prompt = String.format("""
                请分析以下用户输入，判断其意图类型。
                
                用户输入：%s
                
                请从以下类型中选择一个：
                1. EMERGENCY_MEDICAL - 高危医疗紧急情况（如：胸痛、呼吸困难、严重出血、剧烈头痛伴随意识模糊等）
                2. MEDICAL_INQUIRY - 一般医疗咨询（如：轻微症状询问、健康建议等）
                3. GENERAL_CHAT - 通用聊天（如：问候、闲聊、非医疗话题等）
                
                请只返回以下之一：EMERGENCY_MEDICAL、MEDICAL_INQUIRY、GENERAL_CHAT
                不要返回其他任何内容。
                """, userInput);
        
        String intentResult = chatLanguageModel.chat(prompt);
        IntentType intentType = parseIntentType(intentResult);
        
        state.setIntentType(intentType);
        
        // 如果是紧急医疗，直接设置为高风险
        if (intentType == IntentType.EMERGENCY_MEDICAL) {
            state.setRiskLevel(RiskLevel.HIGH);
        }
        
        log.info("IntentRouterNode: 识别意图为 - {}", intentType);
        
        return CompletableFuture.completedFuture(state.data());
    }
    
    /**
     * 解析意图类型
     */
    private IntentType parseIntentType(String result) {
        String normalized = result.trim().toUpperCase();
        
        if (normalized.contains("EMERGENCY_MEDICAL") || normalized.contains("EMERGENCY")) {
            return IntentType.EMERGENCY_MEDICAL;
        } else if (normalized.contains("MEDICAL_INQUIRY") || normalized.contains("MEDICAL")) {
            return IntentType.MEDICAL_INQUIRY;
        } else if (normalized.contains("GENERAL_CHAT") || normalized.contains("GENERAL")) {
            return IntentType.GENERAL_CHAT;
        }
        
        return IntentType.UNKNOWN;
    }
}

