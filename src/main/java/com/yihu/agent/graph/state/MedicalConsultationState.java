package com.yihu.agent.graph.state;


import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicalConsultationState extends AgentState {

    // 1. 定义 Schema（状态结构）
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            "userInput", Channels.base(()->"default user input") ,// 用户输入
            "modelResponse", Channels.base(()->"default model response") ,// 模型响应
            "messages", Channels.appender(ArrayList::new) ,// 消息列表
            "intent", Channels.base(()->"default intent") // 意图

    );
    // 2. 构造函数
    public MedicalConsultationState(Map<String, Object> initData) {
        super(initData);
    }
    // 3. getter

    // 3. 添加便捷访问方法
    public String userInput() {
        return this.<String>value("userInput").orElse("");
    }
    public String modelResponse() {
        return this.<String>value("modelResponse").orElse("");
    }
    public List<String> messages() {
        return this.<List<String>>value("messages").orElse(new ArrayList<>());
    }
    
    public String intent() {
        return this.<String>value("intent").orElse("");
    }
}
