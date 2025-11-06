package com.yihu.agent.controller;

import com.yihu.agent.graph.MedicalConsultationGraph;
import com.yihu.agent.graph.state.MedicalConsultationState;
import com.yihu.agent.service.IntentRecognitionService;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 医疗咨询图测试控制器
 * 用于测试医疗咨询状态图的完整流程
 */
@Slf4j
@RestController
@RequestMapping("/api/medical-consultation")
public class MedicalConsultationController {

    @Autowired
    private IntentRecognitionService intentRecognitionService;

    /**
     * 测试医疗咨询图
     * 
     * @param request 包含用户输入的请求
     * @return 图执行结果
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testMedicalConsultation(
            @RequestBody Map<String, String> request) {
        
        String userInput = request.getOrDefault("userInput", "");
        
        if (userInput.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "userInput 不能为空");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("开始处理医疗咨询请求，用户输入: {}", userInput);
            
            // 构建图
            CompiledGraph<MedicalConsultationState> graph = 
                    MedicalConsultationGraph.buildGraph(intentRecognitionService);
            
            // 执行图
            Map<String, Object> inputData = Map.of("userInput", userInput);
            
            // 记录执行过程
            List<String> executedNodes = new ArrayList<>();
            
            // 使用 stream 方式获取执行过程
            Optional<MedicalConsultationState> finalState = Optional.empty();
            for (NodeOutput<MedicalConsultationState> output : graph.stream(inputData)) {
                String nodeName = output.node();
                executedNodes.add(nodeName);
                log.info("节点执行: {}", nodeName);
                log.info("状态: {}", output.state().data());
                finalState = Optional.of(output.state());
            }
            
            if (finalState.isPresent()) {
                MedicalConsultationState state = finalState.get();
                
                response.put("success", true);
                response.put("userInput", userInput);
                response.put("intent", state.intent());
                response.put("modelResponse", state.modelResponse());
                response.put("messages", state.messages());
                response.put("executedNodes", executedNodes);
                response.put("finalState", state.data());
                
                log.info("医疗咨询处理完成，意图: {}, 响应: {}", 
                        state.intent(), state.modelResponse());
            } else {
                response.put("success", false);
                response.put("error", "图执行失败，未返回最终状态");
            }
            
        } catch (GraphStateException e) {
            log.error("图构建或执行失败: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "图执行异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            log.error("处理医疗咨询请求时发生异常: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "处理异常: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取图结构（Mermaid 格式）
     * 
     * @return 图结构描述
     */
    @GetMapping("/graph-structure")
    public ResponseEntity<Map<String, Object>> getGraphStructure() {
        Map<String, Object> response = new HashMap<>();
        
        // 返回图结构信息
        response.put("success", true);
        response.put("description", "医疗咨询状态图");
        response.put("nodes", new String[]{
                "processUserInput", 
                "intentRecognition", 
                "generalChat", 
                "highRiskMedical", 
                "lowRiskMedical"
        });
        response.put("flow", "START -> processUserInput -> intentRecognition -> " +
                "[generalChat | highRiskMedical | lowRiskMedical] -> END");
        response.put("intentTypes", new String[]{
                "general_chat - 普通对话",
                "high_risk_medical - 高危医疗",
                "low_risk_medical - 非高危医疗"
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "医疗咨询图服务");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}

