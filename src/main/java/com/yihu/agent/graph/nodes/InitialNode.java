package com.yihu.agent.graph.nodes;

import com.yihu.agent.graph.state.MedicalConsultationState;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

public class InitialNode implements NodeAction<MedicalConsultationState> {

    @Override
    public Map<String, Object> apply(MedicalConsultationState medicalConsultationState) throws Exception {
        System.out.println("InitialNode 执行中...");
        System.out.println("用户输入: " + medicalConsultationState.userInput());
        return medicalConsultationState.data();
    }
}
