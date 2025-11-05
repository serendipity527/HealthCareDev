package com.yihu.agent.graph;

import com.yihu.agent.graph.nodes.*;
import com.yihu.agent.graph.state.MedicalConsultationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * 医疗咨询状态图配置
 * 使用LangGraph4j构建完整的医疗咨询流程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalConsultationGraph {
    
    private final InitialNode initialNode;
    private final IntentRouterNode intentRouterNode;
    private final EmergencyResponseNode emergencyResponseNode;
    private final GeneralChatNode generalChatNode;
    private final InformationGatheringNode informationGatheringNode;
    private final SafetyCheckAndRecommendationNode safetyCheckAndRecommendationNode;
    private final SaveSummaryNode saveSummaryNode;
    
    // 节点名称常量
    private static final String NODE_INITIAL = "initial";
    private static final String NODE_INTENT_ROUTER = "intent_router";
    private static final String NODE_EMERGENCY_RESPONSE = "emergency_response";
    private static final String NODE_GENERAL_CHAT = "general_chat";
    private static final String NODE_INFORMATION_GATHERING = "information_gathering";
    private static final String NODE_SAFETY_CHECK = "safety_check";
    private static final String NODE_SAVE_SUMMARY = "save_summary";
    
    /**
     * 构建状态图
     */
    public StateGraph<AgentState> buildGraph() throws Exception {
        log.info("开始构建医疗咨询状态图");
        
        // 创建状态图 - 使用AgentState作为基础类型
        var workflow = new StateGraph<>(AgentState::new);
        
        // 添加节点
        workflow.addNode(NODE_INITIAL, state -> {
            log.debug("执行节点: {}", NODE_INITIAL);
            MedicalConsultationState medState = new MedicalConsultationState(state.data());
            return initialNode.process(medState);
        });
        
        workflow.addNode(NODE_INTENT_ROUTER, state -> {
            log.debug("执行节点: {}", NODE_INTENT_ROUTER);
            MedicalConsultationState medState = new MedicalConsultationState(state.data());
            return intentRouterNode.process(medState);
        });
        
        workflow.addNode(NODE_EMERGENCY_RESPONSE, state -> {
            log.debug("执行节点: {}", NODE_EMERGENCY_RESPONSE);
            MedicalConsultationState medState = new MedicalConsultationState(state.data());
            return emergencyResponseNode.process(medState);
        });
        
        workflow.addNode(NODE_GENERAL_CHAT, state -> {
            log.debug("执行节点: {}", NODE_GENERAL_CHAT);
            MedicalConsultationState medState = new MedicalConsultationState(state.data());
            return generalChatNode.process(medState);
        });
        
        workflow.addNode(NODE_INFORMATION_GATHERING, state -> {
            log.debug("执行节点: {}", NODE_INFORMATION_GATHERING);
            MedicalConsultationState medState = new MedicalConsultationState(state.data());
            return informationGatheringNode.process(medState);
        });
        
        workflow.addNode(NODE_SAFETY_CHECK, state -> {
            log.debug("执行节点: {}", NODE_SAFETY_CHECK);
            MedicalConsultationState medState = new MedicalConsultationState(state.data());
            return safetyCheckAndRecommendationNode.process(medState);
        });
        
        workflow.addNode(NODE_SAVE_SUMMARY, state -> {
            log.debug("执行节点: {}", NODE_SAVE_SUMMARY);
            MedicalConsultationState medState = new MedicalConsultationState(state.data());
            return saveSummaryNode.process(medState);
        });
        
        // 添加边（定义节点间的流转）
        
        // START -> Initial
        workflow.addEdge(START, NODE_INITIAL);
        
        // Initial -> IntentRouter
        workflow.addEdge(NODE_INITIAL, NODE_INTENT_ROUTER);
        
        // IntentRouter -> 根据意图类型路由
        workflow.addEdge(NODE_INTENT_ROUTER, NODE_INFORMATION_GATHERING); // 暂时使用简单边来测试
        
        // EmergencyResponse -> END
        workflow.addEdge(NODE_EMERGENCY_RESPONSE, END);
        
        // GeneralChat -> END
        workflow.addEdge(NODE_GENERAL_CHAT, END);
        
        // InformationGathering -> SafetyCheck
        workflow.addEdge(NODE_INFORMATION_GATHERING, NODE_SAFETY_CHECK); // 暂时使用简单边来测试
        
        // SafetyCheck -> SaveSummary
        workflow.addEdge(NODE_SAFETY_CHECK, NODE_SAVE_SUMMARY);
        
        // SaveSummary -> END
        workflow.addEdge(NODE_SAVE_SUMMARY, END);
        
        log.info("医疗咨询状态图构建完成");
        
        return workflow;
    }
}
