package com.yihu.agent.config;

import com.yihu.agent.graph.MedicalConsultationGraph;
import com.yihu.agent.graph.state.MedicalConsultationState;
import com.yihu.agent.service.IntentRecognitionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.studio.LangGraphStudioServer;
import org.bsc.langgraph4j.studio.springboot.LangGraphStudioConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;


import org.bsc.langgraph4j.StateGraph;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Slf4j
@Configuration
public class HealthLangGraphStudioConfig extends LangGraphStudioConfig {

    @Resource
    IntentRecognitionService intentRecognitionService;
    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        // -----------------
        StateGraph<MedicalConsultationState> graph = null;
        try {
            graph = MedicalConsultationGraph.buildGraphWithNoCompile(intentRecognitionService);
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
        var instance = LangGraphStudioServer.Instance.builder()
                .title("Health Agent Studio") // 可以改个更相关的名字
                .graph(graph)
                .build();

        return Map.of("default", instance);
    }
}