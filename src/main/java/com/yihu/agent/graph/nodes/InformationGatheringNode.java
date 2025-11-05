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
 * InformationGathering节点 - 信息收集与动态风险重评估
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InformationGatheringNode {
    
    private final OpenAiChatModel chatLanguageModel;
    
    /**
     * 收集信息并重新评估风险
     */
    public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
        log.info("InformationGatheringNode: 开始信息收集与风险评估");
        
        // 提取症状信息
        extractSymptoms(state);
        
        // 重新评估风险等级
        reassessRisk(state);
        
        // 判断是否需要继续收集信息
        if (state.getRiskLevel() == RiskLevel.HIGH) {
            // 风险升级为高危，需要转到紧急响应
            log.warn("InformationGatheringNode: 风险升级为高危！");
            state.setNeedMoreInfo(false);
        } else if (state.hasReachedMaxQuestions()) {
            // 达到最大问题数，停止收集
            log.info("InformationGatheringNode: 已达到最大问题数，停止收集");
            state.setNeedMoreInfo(false);
            if (state.getRiskLevel() == RiskLevel.UNASSESSED) {
                state.setRiskLevel(RiskLevel.LOW);
            }
        } else {
            // 继续收集信息
            String question = generateNextQuestion(state);
            if (question != null && !question.trim().isEmpty()) {
                state.setResponse(question);
                state.addToHistory("小医", question);
                state.incrementQuestionCount();
                state.setNeedMoreInfo(true);
                log.info("InformationGatheringNode: 生成下一个问题");
            } else {
                // 信息收集完成
                state.setNeedMoreInfo(false);
                if (state.getRiskLevel() == RiskLevel.UNASSESSED) {
                    state.setRiskLevel(RiskLevel.LOW);
                }
            }
        }
        
        log.info("InformationGatheringNode: 当前风险等级 - {}, 需要更多信息 - {}", 
                state.getRiskLevel(), state.isNeedMoreInfo());
        
        return CompletableFuture.completedFuture(state.data());
    }
    
    /**
     * 提取症状信息
     */
    private void extractSymptoms(String userInput) {
        String prompt = String.format("""
                请从以下用户描述中提取所有症状信息，每个症状单独列出。
                
                用户描述：%s
                
                请以列表形式返回，每行一个症状，格式：
                - 症状1
                - 症状2
                
                如果没有明确的症状，返回"无明确症状"。
                """, userInput);
        
        String extracted = chatLanguageModel.chat(prompt);
        log.debug("提取的症状：{}", extracted);
    }
    
    /**
     * 提取症状信息
     */
    private void extractSymptoms(MedicalConsultationState state) {
        String userInput = state.getUserInput();
        
        String prompt = String.format("""
                请从以下用户描述中提取所有症状信息。
                
                用户描述：%s
                
                已收集的症状：%s
                
                请提取新的症状，如果有多个症状，用逗号分隔。
                如果没有新症状，返回"无"。
                只返回症状名称，不要其他说明文字。
                """, userInput, state.getSymptoms());
        
        String extracted = chatLanguageModel.chat(prompt).trim();
        
        if (!extracted.equals("无") && !extracted.isEmpty()) {
            String[] symptoms = extracted.split("[,，]");
            for (String symptom : symptoms) {
                state.addSymptom(symptom.trim());
            }
        }
        
        log.debug("当前收集的症状：{}", state.getSymptoms());
    }
    
    /**
     * 重新评估风险等级
     */
    private void reassessRisk(MedicalConsultationState state) {
        if (state.getSymptoms().isEmpty()) {
            log.debug("暂无症状信息，风险等级保持不变");
            return;
        }
        
        String prompt = String.format("""
                作为医疗风险评估系统，请评估以下情况的风险等级。
                
                用户描述：%s
                
                已知症状：%s
                
                额外信息：%s
                
                请判断风险等级：
                - HIGH: 高危，需要立即就医（如：持续胸痛、呼吸困难、剧烈头痛伴视力模糊、大量出血等）
                - MEDIUM: 中危，需要密切关注（如：持续发热、剧烈疼痛、症状加重等）
                - LOW: 低危，可以给予建议（如：轻微不适、一般性咨询等）
                
                请只返回：HIGH、MEDIUM 或 LOW
                不要返回其他任何内容。
                """, 
                state.getUserInput(),
                state.getSymptoms(),
                state.getAdditionalInfo()
        );
        
        String riskResult = chatLanguageModel.chat(prompt).trim().toUpperCase();
        
        RiskLevel newRiskLevel = parseRiskLevel(riskResult);
        state.setRiskLevel(newRiskLevel);
        
        log.info("风险评估结果：{}", newRiskLevel);
    }
    
    /**
     * 生成下一个问题
     */
    private String generateNextQuestion(MedicalConsultationState state) {
        String prompt = String.format("""
                作为医疗咨询助手，你正在收集患者信息。
                
                用户初始描述：%s
                
                已收集症状：%s
                
                已问问题数：%d / %d
                
                对话历史：
                %s
                
                请生成一个简洁、专业的问题，以收集更多关键信息（如：
                - 症状持续时间
                - 症状严重程度
                - 是否有其他伴随症状
                - 是否有既往病史
                - 是否正在服药
                等）
                
                如果信息已经足够，返回"INFO_COMPLETE"。
                否则，直接返回问题，不要有其他说明文字。
                """, 
                state.getUserInput(),
                state.getSymptoms(),
                state.getQuestionCount(),
                state.getMaxQuestions(),
                String.join("\n", state.getConversationHistory())
        );
        
        String question = chatLanguageModel.chat(prompt).trim();
        
        if (question.contains("INFO_COMPLETE")) {
            return null;
        }
        
        return question;
    }
    
    /**
     * 解析风险等级
     */
    private RiskLevel parseRiskLevel(String result) {
        if (result.contains("HIGH")) {
            return RiskLevel.HIGH;
        } else if (result.contains("MEDIUM")) {
            return RiskLevel.MEDIUM;
        } else if (result.contains("LOW")) {
            return RiskLevel.LOW;
        }
        return RiskLevel.LOW; // 默认低风险
    }
}

