package com.yihu.agent.graph;

import com.yihu.agent.graph.state.MedicalConsultationState;
import com.yihu.agent.service.IntentRecognitionService;
import org.bsc.langgraph4j.CompiledGraph;
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
     * 创建意图识别节点：使用 AiService 识别用户意图（普通对话、高危医疗、非高危医疗）
     */
    static AsyncNodeAction<MedicalConsultationState> createIntentRecognitionNode(IntentRecognitionService intentService) {
        return node_async(state -> {
            System.out.println("🔍 意图识别节点执行中（使用大模型）...");
            String userInput = state.userInput();
            
            try {
                // 使用 AiService 进行意图识别
                String intent = intentService.recognizeIntent(userInput);
                
                // 清理响应，提取意图类型（确保返回标准格式）
                intent = extractIntent(intent);
                
                System.out.println("✅ 大模型识别到的意图: " + intent);
                
                Map<String, Object> result = new HashMap<>();
                result.put("intent", intent);
                result.put("messages", "意图识别完成: " + intent);
                return result;
            } catch (Exception e) {
                log.error("大模型意图识别失败: {}", e.getMessage(), e);
                // 降级到默认意图：普通对话
                String intent = "general_chat";
                System.out.println("⚠️ 大模型调用失败，使用默认意图: " + intent);
                
                Map<String, Object> result = new HashMap<>();
                result.put("intent", intent);
                result.put("messages", "意图识别完成（降级）: " + intent);
                return result;
            }
        });
    }
    
    /**
     * 从大模型响应中提取意图类型（确保返回标准格式）
     */
    private static String extractIntent(String response) {
        if (response == null || response.isEmpty()) {
            return "general_chat";
        }
        
        String lowerResponse = response.toLowerCase().trim();
        
        // 检查是否包含意图关键词
        if (lowerResponse.contains("high_risk_medical") || 
            lowerResponse.contains("highriskmedical") ||
            lowerResponse.contains("高危医疗")) {
            return "high_risk_medical";
        } else if (lowerResponse.contains("low_risk_medical") || 
                   lowerResponse.contains("lowriskmedical") ||
                   lowerResponse.contains("非高危医疗") ||
                   lowerResponse.contains("一般医疗")) {
            return "low_risk_medical";
        } else if (lowerResponse.contains("general_chat") || 
                   lowerResponse.contains("generalchat") ||
                   lowerResponse.contains("普通对话")) {
            return "general_chat";
        }
        
        // 如果没有明确匹配，尝试通过关键词判断
        if (lowerResponse.contains("胸痛") || lowerResponse.contains("呼吸困难") || 
            lowerResponse.contains("昏迷") || lowerResponse.contains("大出血") ||
            lowerResponse.contains("心脏") || lowerResponse.contains("猝死") ||
            lowerResponse.contains("急性") || lowerResponse.contains("紧急")) {
            return "high_risk_medical";
        } else if (lowerResponse.contains("感冒") || lowerResponse.contains("头疼") || 
                   lowerResponse.contains("咳嗽") || lowerResponse.contains("发烧") ||
                   lowerResponse.contains("症状") || lowerResponse.contains("咨询") ||
                   lowerResponse.contains("治疗") || lowerResponse.contains("药")) {
            return "low_risk_medical";
        }
        
        // 默认返回普通对话
        return "general_chat";
    }

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
     * 构建医疗咨询图（使用大模型进行意图识别）
     * 
     * @param intentService 意图识别服务，使用 @AiService 自动注入
     */
    public static CompiledGraph<MedicalConsultationState> buildGraph(IntentRecognitionService intentService) throws GraphStateException {
        return new StateGraph<>(MedicalConsultationState.SCHEMA, MedicalConsultationState::new)
                // 添加节点
                .addNode("processUserInput", processUserInputNode)                          // 处理用户输入
                .addNode("intentRecognition", createIntentRecognitionNode(intentService))  // 意图识别（使用大模型）
                .addNode("generalChat", generalChatNode)                               // 普通对话
                .addNode("highRiskMedical", highRiskMedicalNode)                        // 高危医疗
                .addNode("lowRiskMedical", lowRiskMedicalNode)                          // 非高危医疗
                
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
        // 注意：在 Spring Boot 环境中，应该通过依赖注入获取 IntentRecognitionService
        // 这里仅作为示例，实际使用时应该从 Spring 容器中获取
        System.out.println("⚠️ 注意：此 main 方法需要 Spring Boot 上下文才能运行");
        System.out.println("   在实际使用中，应该通过 Spring Boot 的依赖注入获取 IntentRecognitionService");
        System.out.println("   例如：在 Service 或 Controller 中注入 IntentRecognitionService，然后调用 buildGraph(intentService)");
        
        // 示例：如何在 Spring Boot Service 中使用
        /*
        @Service
        public class MedicalConsultationService {
            @Autowired
            private IntentRecognitionService intentRecognitionService;
            
            public void processUserInput(String userInput) {
                try {
                    var graph = MedicalConsultationGraph.buildGraph(intentRecognitionService);
                    var result = graph.invoke(Map.of("userInput", userInput));
                    // 处理结果...
                } catch (GraphStateException e) {
                    // 处理异常...
                }
            }
        }
        */
    }
}
