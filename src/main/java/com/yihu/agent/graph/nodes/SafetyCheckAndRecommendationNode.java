package com.yihu.agent.graph.nodes;

import com.yihu.agent.graph.enums.RiskLevel;
import com.yihu.agent.graph.state.MedicalConsultationState;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SafetyCheckAndRecommendation节点 - 安全检查与生成推荐
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafetyCheckAndRecommendationNode {
    
    private final OpenAiChatModel chatLanguageModel;
    
    /**
     * 执行安全检查并生成推荐
     */
    public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
        log.info("SafetyCheckAndRecommendationNode: 开始安全检查与生成推荐");
        
        // 执行安全检查清单
        performSafetyChecks(state);
        
        // 生成推荐内容（可以查询RAG和数据库，这里简化为使用LLM）
        String recommendation = generateRecommendation(state);
        
        state.setRecommendation(recommendation);
        state.setResponse(recommendation);
        state.addToHistory("小医", recommendation);
        
        log.info("SafetyCheckAndRecommendationNode: 推荐已生成");
        
        return CompletableFuture.completedFuture(state.data());
    }
    
    /**
     * 执行安全检查清单
     */
    private void performSafetyChecks(MedicalConsultationState state) {
        log.debug("执行安全检查清单");
        
        // 检查1：是否有紧急症状
        boolean hasEmergencySymptoms = checkEmergencySymptoms(state);
        state.getSafetyCheckResults().put("无紧急症状", !hasEmergencySymptoms);
        
        // 检查2：是否收集了足够信息
        boolean hasSufficientInfo = !state.getSymptoms().isEmpty();
        state.getSafetyCheckResults().put("信息充足", hasSufficientInfo);
        
        // 检查3：风险等级是否合理
        boolean riskLevelAppropriate = state.getRiskLevel() != RiskLevel.HIGH;
        state.getSafetyCheckResults().put("风险等级适当", riskLevelAppropriate);
        
        log.debug("安全检查结果：{}", state.getSafetyCheckResults());
    }
    
    /**
     * 检查是否有紧急症状
     */
    private boolean checkEmergencySymptoms(MedicalConsultationState state) {
        if (state.getSymptoms().isEmpty()) {
            return false;
        }
        
        String prompt = String.format("""
                请判断以下症状中是否包含紧急症状：
                
                症状列表：%s
                
                紧急症状包括但不限于：
                - 胸痛、胸闷
                - 呼吸困难
                - 剧烈头痛
                - 视力突然模糊或丧失
                - 意识模糊
                - 大量出血
                - 严重外伤
                
                如果包含紧急症状，返回"YES"，否则返回"NO"。
                只返回YES或NO，不要其他内容。
                """, state.getSymptoms());
        
        String result = chatLanguageModel.chat(prompt).trim().toUpperCase();
        return result.contains("YES");
    }
    
    /**
     * 生成推荐内容
     */
    private String generateRecommendation(MedicalConsultationState state) {
        String prompt = String.format("""
                作为专业的医疗咨询AI助手，请根据以下信息生成专业、实用的健康建议。
                
                用户咨询：%s
                
                症状信息：%s
                
                风险等级：%s
                
                额外信息：%s
                
                安全检查结果：%s
                
                请生成一份包含以下内容的建议：
                
                1. 【症状分析】- 简要分析可能的原因
                2. 【建议措施】- 具体的应对建议（生活方式、饮食、休息等）
                3. 【就医建议】- 是否需要就医及就医时机
                4. 【注意事项】- 需要注意观察的情况
                5. 【免责声明】- 提醒这只是AI建议，不能替代专业医疗诊断
                
                请使用友好、专业的语气，让用户感到安心。
                """, 
                state.getUserInput(),
                state.getSymptoms().isEmpty() ? "无明确症状" : state.getSymptoms(),
                state.getRiskLevel(),
                state.getAdditionalInfo().isEmpty() ? "无" : state.getAdditionalInfo(),
                state.getSafetyCheckResults()
        );
        
        return chatLanguageModel.chat(prompt);
    }
}

