package com.yihu.agent.graph;

import com.yihu.agent.graph.nodes.InitialNode;
import com.yihu.agent.graph.state.MedicalConsultationState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class MedicalConsultationGraph {
    public static void main(String[] args) throws Exception {
        StateGraph<MedicalConsultationState> stateGraph = new StateGraph<>(MedicalConsultationState::new);

        InitialNode initialNode = new InitialNode();
        stateGraph.addNode("initialNode",node_async(initialNode));
        stateGraph.addNode("InitialNode", node_async(state -> {
            System.out.println("InitialNode 执行中...");
            System.out.println("用户名: " + state.userName());

            // 返回状态更新
            return Map.of(
                    "greeting", "你好，" + state.userName() + "！"
            );
        }));

        // 添加边：START -> InitialNode -> END
        stateGraph.addEdge(START, "InitialNode");
        stateGraph.addEdge("InitialNode", END);

        CompiledGraph<MedicalConsultationState> compile = stateGraph.compile();
        compile.invoke(Map.of("userName","张三"));
        GraphRepresentation graph = compile.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(graph);


    }

}
