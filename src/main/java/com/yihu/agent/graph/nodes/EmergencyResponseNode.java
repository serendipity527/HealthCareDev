package com.yihu.agent.graph.nodes;

import com.yihu.agent.graph.state.MedicalConsultationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * EmergencyResponse节点 - 处理高危医疗情况，强制阻断并提供紧急指引
 */
@Slf4j
@Component
public class EmergencyResponseNode {
    
    /**
     * 处理紧急医疗情况
     */
    public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
        log.warn("EmergencyResponseNode: 检测到高危医疗情况！");
        
        // 生成紧急响应
        String emergencyResponse = generateEmergencyResponse(state);
        
        state.setResponse(emergencyResponse);
        state.addToHistory("系统", emergencyResponse);
        
        // 创建紧急病历摘要
        state.setMedicalSummary(String.format(
                "⚠️ 紧急医疗咨询记录\n" +
                "时间: %s\n" +
                "用户输入: %s\n" +
                "风险等级: %s\n" +
                "症状: %s\n" +
                "处理: 已提供紧急就医指引",
                java.time.LocalDateTime.now(),
                state.getUserInput(),
                state.getRiskLevel(),
                state.getSymptoms()
        ));
        
        log.info("EmergencyResponseNode: 紧急响应已生成");
        
        return CompletableFuture.completedFuture(state.data());
    }
    
    /**
     * 生成紧急响应内容
     */
    private String generateEmergencyResponse(MedicalConsultationState state) {
        return String.format("""
                ⚠️⚠️⚠️ 紧急医疗提醒 ⚠️⚠️⚠️
                
                根据您描述的症状，这可能是需要立即医疗干预的紧急情况！
                
                ⚠️ 请立即采取以下行动：
                
                1. 🚨 立即拨打急救电话：120
                2. 🏥 或前往最近的医院急诊科
                3. 👥 如有可能，请寻求他人帮助
                4. ⏰ 不要等待症状自行缓解
                
                ⚠️ 在等待急救时：
                - 保持冷静，尽量放松
                - 如有他人在场，请告知您的症状
                - 不要独自驾车前往医院
                - 保持手机畅通
                
                您的症状：%s
                
                ⚠️ 重要提示：我是AI助手，不能替代专业医疗诊断。
                上述建议仅供参考，请务必寻求专业医疗帮助！
                """, state.getUserInput());
    }
}

