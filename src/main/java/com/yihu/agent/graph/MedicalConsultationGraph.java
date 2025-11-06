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
import org.bsc.langgraph4j.action.AsyncNodeAction;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MedicalConsultationGraph {
    static AsyncNodeAction<MedicalConsultationState> receiveUserInputNode = node_async(state -> {
        // 接收用户输入节点，负责获取用户输入并输出到控制台，同时返回一个包含用户输入的Map
        System.out.println("receiveUserInputNode 执行中...");   // 打印节点执行提示
        String userInput = state.userInput();                   // 获取用户输入
        System.out.println("用户输入: " + userInput);            // 打印用户输入
        return Map.of("userInput", userInput,
        "messages", userInput);                  // 返回用户输入，更新状态
    });

    static AsyncNodeAction<MedicalConsultationState> callLLMNode = node_async(state -> {
        System.out.println("callLLMNode 执行中...");
        List<String> messages = state.messages();
        log.info("messages: {}", messages);

        String userInput = state.userInput();

        String modelResponse = callLLM(userInput);

        return Map.of("modelResponse", modelResponse,
        "messages", modelResponse);
    });
    private static String callLLM(String userInput) {
        return "模拟的模型响应";
    }
    public static void main(String[] args) throws Exception {

        // 创建状态图
        StateGraph<MedicalConsultationState> stateGraph = new StateGraph<>(MedicalConsultationState.SCHEMA, MedicalConsultationState::new);

        stateGraph.addNode("receiveUserInputNode", receiveUserInputNode);
        stateGraph.addNode("callLLMNode", callLLMNode);

        stateGraph.addEdge(START, "receiveUserInputNode");
        stateGraph.addEdge("receiveUserInputNode", "callLLMNode");
        stateGraph.addEdge("callLLMNode", END);

        CompiledGraph<MedicalConsultationState> compile = stateGraph.compile();
        // compile.invoke(Map.of("userInput", "张三"));
        GraphRepresentation graph = compile.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println(graph);

        for (var output : compile.stream(Map.of("userInput", "张三"))) {
            System.out.println("节点: " + output.node());
            System.out.println("状态: " + output.state().data());
            System.out.println("---");
        }   
    }
}
