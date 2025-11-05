package com.yihu.agent.graph.state;

import com.yihu.agent.graph.enums.IntentType;
import com.yihu.agent.graph.enums.RiskLevel;
import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医疗咨询状态类
 * 用于在状态图节点间传递和维护会话状态
 */
public class MedicalConsultationState extends AgentState {
    
    // 状态字段键名常量
    public static final String USER_ID = "userId";
    public static final String USER_INPUT = "userInput";
    public static final String CONVERSATION_HISTORY = "conversationHistory";
    public static final String INTENT_TYPE = "intentType";
    public static final String RISK_LEVEL = "riskLevel";
    public static final String SYMPTOMS = "symptoms";
    public static final String ADDITIONAL_INFO = "additionalInfo";
    public static final String RESPONSE = "response";
    public static final String NEED_MORE_INFO = "needMoreInfo";
    public static final String QUESTION_COUNT = "questionCount";
    public static final String MAX_QUESTIONS = "maxQuestions";
    public static final String SAFETY_CHECK_RESULTS = "safetyCheckResults";
    public static final String RECOMMENDATION = "recommendation";
    public static final String MEDICAL_SUMMARY = "medicalSummary";
    public static final String SAVED = "saved";
    
    /**
     * 构造函数
     */
    public MedicalConsultationState(Map<String, Object> initData) {
        super(initData);
    }
    
    /**
     * 便捷的构造函数
     */
    public MedicalConsultationState() {
        super(createDefaultData());
    }
    
    /**
     * 创建默认数据
     */
    private static Map<String, Object> createDefaultData() {
        Map<String, Object> data = new HashMap<>();
        data.put(CONVERSATION_HISTORY, new ArrayList<String>());
        data.put(RISK_LEVEL, RiskLevel.UNASSESSED);
        data.put(SYMPTOMS, new ArrayList<String>());
        data.put(ADDITIONAL_INFO, new HashMap<String, String>());
        data.put(NEED_MORE_INFO, false);
        data.put(QUESTION_COUNT, 0);
        data.put(MAX_QUESTIONS, 5);
        data.put(SAFETY_CHECK_RESULTS, new HashMap<String, Boolean>());
        data.put(SAVED, false);
        return data;
    }
    
    // Getter 和 Setter 方法（通过 data() 访问内部数据）
    public String getUserId() {
        return this.<String>value(USER_ID).orElse(null);
    }
    
    public void setUserId(String userId) {
        // 需要创建新的Map来更新，因为AgentState内部的data是不可变的
        Map<String, Object> updates = new HashMap<>();
        updates.put(USER_ID, userId);
        mergeData(updates);
    }
    
    public String getUserInput() {
        return this.<String>value(USER_INPUT).orElse(null);
    }
    
    public void setUserInput(String userInput) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(USER_INPUT, userInput);
        mergeData(updates);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getConversationHistory() {
        return this.<List<String>>value(CONVERSATION_HISTORY).orElse(new ArrayList<>());
    }
    
    public void setConversationHistory(List<String> history) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(CONVERSATION_HISTORY, history);
        mergeData(updates);
    }
    
    public IntentType getIntentType() {
        return this.<IntentType>value(INTENT_TYPE).orElse(null);
    }
    
    public void setIntentType(IntentType intentType) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(INTENT_TYPE, intentType);
        mergeData(updates);
    }
    
    public RiskLevel getRiskLevel() {
        return this.<RiskLevel>value(RISK_LEVEL).orElse(RiskLevel.UNASSESSED);
    }
    
    public void setRiskLevel(RiskLevel riskLevel) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(RISK_LEVEL, riskLevel);
        mergeData(updates);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getSymptoms() {
        return this.<List<String>>value(SYMPTOMS).orElse(new ArrayList<>());
    }
    
    public void setSymptoms(List<String> symptoms) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(SYMPTOMS, symptoms);
        mergeData(updates);
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, String> getAdditionalInfo() {
        return this.<Map<String, String>>value(ADDITIONAL_INFO).orElse(new HashMap<>());
    }
    
    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(ADDITIONAL_INFO, additionalInfo);
        mergeData(updates);
    }
    
    public String getResponse() {
        return this.<String>value(RESPONSE).orElse(null);
    }
    
    public void setResponse(String response) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(RESPONSE, response);
        mergeData(updates);
    }
    
    public boolean isNeedMoreInfo() {
        return this.<Boolean>value(NEED_MORE_INFO).orElse(false);
    }
    
    public void setNeedMoreInfo(boolean needMoreInfo) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(NEED_MORE_INFO, needMoreInfo);
        mergeData(updates);
    }
    
    public int getQuestionCount() {
        return this.<Integer>value(QUESTION_COUNT).orElse(0);
    }
    
    public void setQuestionCount(int questionCount) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(QUESTION_COUNT, questionCount);
        mergeData(updates);
    }
    
    public int getMaxQuestions() {
        return this.<Integer>value(MAX_QUESTIONS).orElse(5);
    }
    
    public void setMaxQuestions(int maxQuestions) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(MAX_QUESTIONS, maxQuestions);
        mergeData(updates);
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Boolean> getSafetyCheckResults() {
        return this.<Map<String, Boolean>>value(SAFETY_CHECK_RESULTS).orElse(new HashMap<>());
    }
    
    public void setSafetyCheckResults(Map<String, Boolean> safetyCheckResults) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(SAFETY_CHECK_RESULTS, safetyCheckResults);
        mergeData(updates);
    }
    
    public String getRecommendation() {
        return this.<String>value(RECOMMENDATION).orElse(null);
    }
    
    public void setRecommendation(String recommendation) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(RECOMMENDATION, recommendation);
        mergeData(updates);
    }
    
    public String getMedicalSummary() {
        return this.<String>value(MEDICAL_SUMMARY).orElse(null);
    }
    
    public void setMedicalSummary(String medicalSummary) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(MEDICAL_SUMMARY, medicalSummary);
        mergeData(updates);
    }
    
    public boolean isSaved() {
        return this.<Boolean>value(SAVED).orElse(false);
    }
    
    public void setSaved(boolean saved) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(SAVED, saved);
        mergeData(updates);
    }
    
    /**
     * 合并数据到当前状态
     * 使用反射访问父类的私有data字段
     */
    private void mergeData(Map<String, Object> updates) {
        try {
            java.lang.reflect.Field dataField = AgentState.class.getDeclaredField("data");
            dataField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataField.get(this);
            data.putAll(updates);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update state data", e);
        }
    }
    
    
    // 辅助方法
    
    /**
     * 添加对话到历史记录
     */
    public void addToHistory(String role, String message) {
        List<String> history = getConversationHistory();
        history.add(role + ": " + message);
        setConversationHistory(history);
    }
    
    /**
     * 添加症状
     */
    public void addSymptom(String symptom) {
        if (symptom != null && !symptom.trim().isEmpty()) {
            List<String> symptoms = getSymptoms();
            symptoms.add(symptom.trim());
            setSymptoms(symptoms);
        }
    }
    
    /**
     * 添加额外信息
     */
    public void putAdditionalInfo(String key, String value) {
        Map<String, String> info = getAdditionalInfo();
        info.put(key, value);
        setAdditionalInfo(info);
    }
    
    /**
     * 增加问题计数
     */
    public void incrementQuestionCount() {
        int count = getQuestionCount();
        setQuestionCount(count + 1);
    }
    
    /**
     * 检查是否达到最大问题数
     */
    public boolean hasReachedMaxQuestions() {
        return getQuestionCount() >= getMaxQuestions();
    }
    
    /**
     * 创建新的状态实例（Builder模式辅助方法）
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder类
     */
    public static class Builder {
        private final Map<String, Object> data = new HashMap<>();
        
        public Builder userId(String userId) {
            data.put(USER_ID, userId);
            return this;
        }
        
        public Builder userInput(String userInput) {
            data.put(USER_INPUT, userInput);
            return this;
        }
        
        public MedicalConsultationState build() {
            return new MedicalConsultationState(data);
        }
    }
}

