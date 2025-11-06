package com.yihu.agent.graph;

import com.yihu.agent.graph.state.MedicalConsultationState;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncCommandAction;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.Command;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MedicalConsultationGraph {


    /**
     * 处理用户输入节点
     */
    static AsyncNodeAction<MedicalConsultationState> processUserInputNode = node_async(state -> {
        System.out.println("📥 处理用户输入节点执行中...");
        String userInput = state.userInput();
        System.out.println("用户输入: " + userInput);
        
        // 将用户输入添加到消息列表
        return Map.of(
                "userInput", userInput,
                "messages", userInput
        );
    });

    /**
     * 意图识别节点：识别用户意图（普通对话、高危医疗、非高危医疗）
     */
    static AsyncNodeAction<MedicalConsultationState> intentRecognitionNode = node_async(state -> {
        System.out.println("🔍 意图识别节点执行中...");
        String userInput = state.userInput();
        
        // 简单的意图识别逻辑
        String intent;
        String lowerInput = userInput.toLowerCase();
        
        // 高危医疗关键词：胸痛、呼吸困难、昏迷、大出血等
        if (lowerInput.contains("胸痛") || lowerInput.contains("呼吸困难") || 
            lowerInput.contains("昏迷") || lowerInput.contains("大出血") ||
            lowerInput.contains("心脏") || lowerInput.contains("猝死") ||
            lowerInput.contains("急性") || lowerInput.contains("紧急")) {
            intent = "high_risk_medical";
            System.out.println("✅ 识别到的意图: 高危医疗");
        } 
        // 非高危医疗关键词：感冒、头疼、咳嗽、发烧等
        else if (lowerInput.contains("感冒") || lowerInput.contains("头疼") || 
                 lowerInput.contains("咳嗽") || lowerInput.contains("发烧") ||
                 lowerInput.contains("症状") || lowerInput.contains("咨询") ||
                 lowerInput.contains("治疗") || lowerInput.contains("药")) {
            intent = "low_risk_medical";
            System.out.println("✅ 识别到的意图: 非高危医疗");
        } 
        // 普通对话
        else {
            intent = "general_chat";
            System.out.println("✅ 识别到的意图: 普通对话");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);
        result.put("messages", "意图识别完成: " + intent);
        return result;
    });

    /**
     * 普通对话节点
     */
    static AsyncNodeAction<MedicalConsultationState> generalChatNode = node_async(state -> {
        System.out.println("💬 普通对话节点执行中...");
        String response = "您好！我是医疗咨询助手，很高兴为您服务。如果您有医疗相关问题，请告诉我。";
        
        Map<String, Object> result = new HashMap<>();
        result.put("modelResponse", response);
        result.put("messages", response);
        return result;
    });

    /**
     * 高危医疗节点
     */
    static AsyncNodeAction<MedicalConsultationState> highRiskMedicalNode = node_async(state -> {
        System.out.println("⚠️ 高危医疗节点执行中...");
        String response = "⚠️ 紧急提醒：根据您描述的症状，可能存在紧急医疗情况。\n" +
                         "建议您：\n" +
                         "1. 立即拨打120急救电话\n" +
                         "2. 或尽快前往最近的医院急诊科\n" +
                         "3. 不要延误，及时就医非常重要";
        
        Map<String, Object> result = new HashMap<>();
        result.put("modelResponse", response);
        result.put("messages", response);
        return result;
    });

    /**
     * 非高危医疗节点
     */
    static AsyncNodeAction<MedicalConsultationState> lowRiskMedicalNode = node_async(state -> {
        System.out.println("🏥 非高危医疗节点执行中...");
        String response = "根据您描述的症状，建议您：\n" +
                         "1. 注意休息，多喝水\n" +
                         "2. 观察症状变化\n" +
                         "3. 如症状持续或加重，请及时就医\n" +
                         "4. 可以咨询专业医生获取更详细的建议";
        
        Map<String, Object> result = new HashMap<>();
        result.put("modelResponse", response);
        result.put("messages", response);
        return result;
    });

    /**
     * 条件路由函数：根据意图路由到不同的分支
     */
    static AsyncCommandAction<MedicalConsultationState> routeByIntent = (state, config) -> {
        String intent = state.intent();
        System.out.println("🔀 路由决策，当前意图: " + intent);
        
        if (intent.isEmpty()) {
            throw new IllegalStateException("意图未识别");
        }
        
        // 返回 Command，gotoNode 必须是 mappings 中的 key
        return completedFuture(new Command(intent));
    };
    
    /**
     * 构建医疗咨询图
     */
    public static CompiledGraph<MedicalConsultationState> buildGraph() throws GraphStateException {
        return new StateGraph<>(MedicalConsultationState.SCHEMA, MedicalConsultationState::new)
                // 添加节点
                .addNode("processUserInput", processUserInputNode)      // 处理用户输入
                .addNode("intentRecognition", intentRecognitionNode)     // 意图识别
                .addNode("generalChat", generalChatNode)                 // 普通对话
                .addNode("highRiskMedical", highRiskMedicalNode)         // 高危医疗
                .addNode("lowRiskMedical", lowRiskMedicalNode)           // 非高危医疗
                
                // START -> 处理用户输入
                .addEdge(START, "processUserInput")
                
                // 处理用户输入 -> 意图识别
                .addEdge("processUserInput", "intentRecognition")
                
                // 意图识别 -> 三个分支（条件边）
                .addConditionalEdges(
                        "intentRecognition",           // 源节点：意图识别节点
                        routeByIntent,                 // 条件路由函数
                        Map.of(
                                "general_chat", "generalChat",           // 普通对话
                                "high_risk_medical", "highRiskMedical",  // 高危医疗
                                "low_risk_medical", "lowRiskMedical"     // 非高危医疗
                        )
                )
                
                // 三个分支都连接到 END
                .addEdge("generalChat", END)
                .addEdge("highRiskMedical", END)
                .addEdge("lowRiskMedical", END)
                
                // 编译图
                .compile();
    }
    
    public static void main(String[] args) throws Exception {
        var graph = buildGraph();
        
        // 打印图结构
        GraphRepresentation graphRep = graph.getGraph(GraphRepresentation.Type.MERMAID);
        System.out.println("========== 图结构 ==========");
        System.out.println(graphRep);
        System.out.println("\n");
        
        // 测试1: 普通对话
        System.out.println("========== 测试1: 普通对话 ==========");
        for (var output : graph.stream(Map.of("userInput", "你好"))) {
            System.out.println("节点: " + output.node());
            System.out.println("状态: " + output.state().data());
            System.out.println("---");
        }
        
        System.out.println("\n");
        
        // 测试2: 高危医疗
        System.out.println("========== 测试2: 高危医疗 ==========");
        for (var output : graph.stream(Map.of("userInput", "我胸痛，呼吸困难"))) {
            System.out.println("节点: " + output.node());
            System.out.println("状态: " + output.state().data());
            System.out.println("---");
        }
        
        System.out.println("\n");
        
        // 测试3: 非高危医疗
        System.out.println("========== 测试3: 非高危医疗 ==========");
        for (var output : graph.stream(Map.of("userInput", "我有点感冒，头疼"))) {
            System.out.println("节点: " + output.node());
            System.out.println("状态: " + output.state().data());
            System.out.println("---");
        }
    }
}
