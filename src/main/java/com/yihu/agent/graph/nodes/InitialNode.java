package com.yihu.agent.graph.nodes;

import com.yihu.agent.graph.state.MedicalConsultationState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Initial节点 - 接收用户输入并初始化状态
 */
@Slf4j
@Component
public class InitialNode {
    
    /**
     * 处理用户输入，初始化对话状态
     */
    public CompletableFuture<Map<String, Object>> process(MedicalConsultationState state) {
        log.info("InitialNode: 接收用户输入 - {}", state.getUserInput());
        
        // 记录用户输入到历史
        state.addToHistory("用户", state.getUserInput());
        
        // 返回更新后的状态
        return CompletableFuture.completedFuture(state.data());
    }
}

