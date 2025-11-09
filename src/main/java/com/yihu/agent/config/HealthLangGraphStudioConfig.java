package com.yihu.agent.config;

import com.yihu.agent.graph.state.MedicalConsultationState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
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

    @Override
    public Map<String, LangGraphStudioServer.Instance> instanceMap() {
        var workflow = new StateGraph<>(AgentState::new);

        // --- 定义工作流 ---

        // 1. 添加一个节点 (Node)
        // 这里用一个简单的 Lambda 表达式作为示例节点。
        // 在实际业务中，这里通常是你定义的 Agent 或 Tool 调用逻辑。
        try {
            workflow.addNode("agent", node_async(state -> {
                log.info("Agent state: {}", state);
                // 返回要更新的状态部分。如果不需要更新状态，返回空 Map。
                return Map.of();
            }));


        // 2. 【关键修复】添加起始边 (Entry Point) 🎯
        // 这行代码告诉图：启动后，立刻跳转到 "agent" 节点。
        // 如果缺少这一行，就会报 "missing Entry Point"。
        workflow.addEdge(START, "agent");

        // 3. 添加结束边 (可选但推荐)
        // 告诉图：运行完 "agent" 节点后，流程结束。
        workflow.addEdge("agent", END);
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
        // -----------------

        var instance = LangGraphStudioServer.Instance.builder()
                .title("Health Agent Studio") // 可以改个更相关的名字
                .graph(workflow)
                .build();

        return Map.of("default", instance);
    }
}