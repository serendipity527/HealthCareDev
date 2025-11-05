package com.yihu.agent.service;

import com.yihu.agent.graph.MedicalConsultationGraph;
import com.yihu.agent.graph.state.MedicalConsultationState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.GraphRepresentation;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

/**
 * 医疗咨询服务
 * 整合LangGraph4j状态图，提供医疗咨询功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalConsultationService {
    
    private final MedicalConsultationGraph medicalConsultationGraph;
    
    private CompiledGraph<AgentState> compiledGraph;
    
    /**
     * 初始化编译状态图
     */
    @PostConstruct
    public void init() {
        try {
            log.info("初始化医疗咨询状态图...");
            StateGraph<AgentState> stateGraph = medicalConsultationGraph.buildGraph();
            compiledGraph = stateGraph.compile();
            log.info("医疗咨询状态图初始化完成");
            
            // 打印 Mermaid 格式的状态图
            printGraphDiagram();
        } catch (Exception e) {
            log.error("初始化医疗咨询状态图失败", e);
            throw new RuntimeException("Failed to initialize medical consultation graph", e);
        }
    }
    
    /**
     * 打印状态图的 Mermaid 格式
     */
    private void printGraphDiagram() {
        try {
            GraphRepresentation graphRep = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
            String mermaidDiagram = graphRep.getContent();
            log.info("\n" + 
                    "========================================\n" +
                    "医疗咨询状态图 (Mermaid 格式)\n" +
                    "========================================\n" +
                    mermaidDiagram + "\n" +
                    "========================================\n" +
                    "在线预览: https://mermaid.live/\n" +
                    "将以上 Mermaid 代码复制到在线编辑器即可查看图形\n" +
                    "========================================");
        } catch (Exception e) {
            log.warn("打印状态图失败", e);
        }
    }
    
    /**
     * 获取状态图的 Mermaid 格式（供 API 调用）
     */
    public String getGraphDiagram() {
        try {
            GraphRepresentation graphRep = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
            return graphRep.getContent();
        } catch (Exception e) {
            log.error("获取状态图失败", e);
            return "Error: Unable to get graph diagram";
        }
    }
    
    /**
     * 处理用户咨询
     * 
     * @param userId 用户ID
     * @param userInput 用户输入
     * @return 响应内容
     */
    public CompletableFuture<String> processConsultation(String userId, String userInput) {
        log.info("处理用户咨询 - 用户: {}, 输入: {}", userId, userInput);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 创建初始状态
                MedicalConsultationState initialState = MedicalConsultationState.builder()
                        .userId(userId)
                        .userInput(userInput)
                        .build();

                // 执行状态图
                AgentState resultState = compiledGraph.invoke(initialState.data())
                    .orElseThrow(() -> new RuntimeException("状态图执行失败"));
                
                // 从结果中构建状态对象
                MedicalConsultationState result = new MedicalConsultationState(resultState.data());
                
                // 获取响应
                String response = result.getResponse();
                
                if (response == null || response.isEmpty()) {
                    response = "抱歉，处理您的请求时遇到问题，请稍后再试。";
                }
                
                log.info("咨询处理完成 - 用户: {}", userId);
                
                return response;
                
            } catch (Exception e) {
                log.error("处理用户咨询失败 - 用户: {}, 输入: {}", userId, userInput, e);
                return "抱歉，处理您的请求时发生错误：" + e.getMessage();
            }
        });
    }
    
    /**
     * 处理多轮对话
     * 这个方法支持保持会话状态进行多轮对话
     * 
     * @param state 当前会话状态
     * @param userInput 新的用户输入
     * @return 更新后的状态
     */
    public CompletableFuture<MedicalConsultationState> processWithState(
            MedicalConsultationState state, String userInput) {
        
        log.info("处理多轮对话 - 用户: {}, 轮次: {}", state.getUserId(), state.getQuestionCount());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 更新用户输入
                state.setUserInput(userInput);
                
                // 执行状态图
                AgentState resultState = compiledGraph.invoke(state.data())
                    .orElseThrow(() -> new RuntimeException("状态图执行失败"));
                
                // 从结果中构建状态对象
                MedicalConsultationState result = new MedicalConsultationState(resultState.data());
                
                log.info("多轮对话处理完成 - 用户: {}, 轮次: {}", 
                        result.getUserId(), result.getQuestionCount());
                
                return result;
                
            } catch (Exception e) {
                log.error("处理多轮对话失败", e);
                state.setResponse("抱歉，处理您的请求时发生错误：" + e.getMessage());
                return state;
            }
        });
    }
    
    /**
     * 创建新的会话状态
     * 
     * @param userId 用户ID
     * @return 新的会话状态
     */
    public MedicalConsultationState createNewSession(String userId) {
        return MedicalConsultationState.builder()
                .userId(userId)
                .build();
    }
}
