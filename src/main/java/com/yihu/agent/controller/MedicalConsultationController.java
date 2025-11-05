package com.yihu.agent.controller;

import com.yihu.agent.service.MedicalConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 医疗咨询REST API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true", maxAge = 3600)
public class MedicalConsultationController {
    
    private final MedicalConsultationService consultationService;
    
    /**
     * 处理医疗咨询请求
     * 
     * @param request 请求体，包含userId和message
     * @return 咨询响应
     */
    @PostMapping("/ask")
    public CompletableFuture<ResponseEntity<Map<String, String>>> askQuestion(
            @RequestBody Map<String, String> request) {
        
        String userId = request.getOrDefault("userId", "anonymous");
        String message = request.get("message");
        
        if (message == null || message.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                ResponseEntity.badRequest()
                    .body(Map.of("error", "Message cannot be empty"))
            );
        }
        
        log.info("收到咨询请求 - 用户: {}, 消息: {}", userId, message);
        
        return consultationService.processConsultation(userId, message)
                .thenApply(response -> ResponseEntity.ok(Map.of(
                        "userId", userId,
                        "response", response
                )))
                .exceptionally(ex -> {
                    log.error("处理咨询请求失败", ex);
                    return ResponseEntity.internalServerError()
                            .body(Map.of("error", "处理请求失败: " + ex.getMessage()));
                });
    }
    
    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "Medical Consultation Service"
        ));
    }
}

