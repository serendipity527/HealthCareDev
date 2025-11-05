package com.yihu.agent.graph.state;


import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.Map;

public class MedicalConsultationState extends AgentState {

    // 1. 定义 Schema（状态结构）
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            "userName", Channels.base(()->"")
    );
    // 2. 构造函数
    public MedicalConsultationState(Map<String, Object> initData) {
        super(initData);
    }
    // 3. getter

    // 3. 添加便捷访问方法
    public String userName() {
        return this.<String>value("userName").orElse("");
    }
}
