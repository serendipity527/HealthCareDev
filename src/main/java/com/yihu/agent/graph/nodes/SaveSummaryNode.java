package com.yihu.agent.graph.nodes;

import com.yihu.agent.graph.state.MedicalConsultationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * SaveSummary节点 - 保存病历摘要
 */
@Slf4j
@Component
public class SaveSummaryNode {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 保存病历摘要
     */
    public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
        log.info("SaveSummaryNode: 开始保存病历摘要");
        
        // 生成病历摘要
        String summary = generateMedicalSummary(state);
        state.setMedicalSummary(summary);
        
        // 实际应用中，这里应该保存到数据库
        // 这里仅记录日志
        log.info("病历摘要已生成：\n{}", summary);
        
        // 保存成功
        state.setSaved(true);
        
        // 添加保存确认信息到响应（如果需要告知用户）
        if (state.getResponse() == null || state.getResponse().isEmpty()) {
            state.setResponse(state.getRecommendation());
        }
        
        log.info("SaveSummaryNode: 病历摘要保存完成");
        
        return CompletableFuture.completedFuture(state.data());
    }
    
    /**
     * 生成病历摘要
     */
    private String generateMedicalSummary(MedicalConsultationState state) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("=" .repeat(50)).append("\n");
        summary.append("医疗咨询记录摘要\n");
        summary.append("=".repeat(50)).append("\n\n");
        
        summary.append("【基本信息】\n");
        summary.append("用户ID: ").append(state.getUserId() != null ? state.getUserId() : "匿名用户").append("\n");
        summary.append("咨询时间: ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        summary.append("意图类型: ").append(state.getIntentType()).append("\n");
        summary.append("风险等级: ").append(state.getRiskLevel()).append("\n\n");
        
        summary.append("【主诉】\n");
        summary.append(state.getUserInput()).append("\n\n");
        
        if (!state.getSymptoms().isEmpty()) {
            summary.append("【症状列表】\n");
            for (int i = 0; i < state.getSymptoms().size(); i++) {
                summary.append(i + 1).append(". ").append(state.getSymptoms().get(i)).append("\n");
            }
            summary.append("\n");
        }
        
        if (!state.getAdditionalInfo().isEmpty()) {
            summary.append("【额外信息】\n");
            state.getAdditionalInfo().forEach((key, value) -> 
                summary.append("- ").append(key).append(": ").append(value).append("\n")
            );
            summary.append("\n");
        }
        
        if (!state.getSafetyCheckResults().isEmpty()) {
            summary.append("【安全检查】\n");
            state.getSafetyCheckResults().forEach((key, value) -> 
                summary.append("- ").append(key).append(": ").append(value ? "✓" : "✗").append("\n")
            );
            summary.append("\n");
        }
        
        if (state.getRecommendation() != null && !state.getRecommendation().isEmpty()) {
            summary.append("【AI建议】\n");
            summary.append(state.getRecommendation()).append("\n\n");
        }
        
        summary.append("【对话轮次】\n");
        summary.append("问答轮次: ").append(state.getQuestionCount()).append("\n");
        summary.append("对话记录数: ").append(state.getConversationHistory().size()).append("\n\n");
        
        summary.append("=".repeat(50)).append("\n");
        
        return summary.toString();
    }
}

