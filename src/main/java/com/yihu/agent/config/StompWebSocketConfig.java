package com.yihu.agent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import org.springframework.web.util.UrlPathHelper;

import java.util.List;

/**
 * WebSocket 配置类，实现 WebSocketMessageBrokerConfigurer 接口。
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 注册 STOMP 端点，客户端连接时需要使用的端点地址。
     *
     * @param registry StompEndpointRegistry 注册对象
     */
    /**
     * 注册 STOMP 端点，配置 SockJS 支持、拦截路径顺序、路径助手与接收顺序等。
     *
     * @param registry StompEndpointRegistry 注册对象
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 添加 WebSocket 端点（原生 WebSocket）
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*");
        
        // 添加 WebSocket 端点，并启用 SockJS 以兼容不支持 WebSocket 的浏览器
        // 允许所有源的跨域访问（生产环境建议指定具体域名）
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 设置错误处理器，用于处理 WebSocket 错误
        // 设置自定义的STOMP协议错误处理器，用于捕获和记录通过WebSocket传递的错误信息
        registry.setErrorHandler(new StompSubProtocolErrorHandler() {
            @Override
            public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
                // 通过header访问器获取session信息
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(clientMessage);
                // 记录当前session发生的异常信息
                log.error("WebSocket error on session {}: {}", accessor.getSessionId(), ex.getMessage(), ex);
                // 调用父类方法，生成默认的错误帧消息返回给客户端
                return super.handleClientMessageProcessingError(clientMessage, ex);
            }
        });
        // 设置拦截器顺序，1 表示优先级较高
        registry.setOrder(1);

        // 设置 URL 路径助手，便于处理路径参数
        registry.setUrlPathHelper(new UrlPathHelper());

        // 开启接收顺序保护，防止乱序
        registry.setPreserveReceiveOrder(true);
    }

    /**
     * 配置 WebSocket 传输相关参数，如消息大小限制、缓冲区大小限制、连接超时时间等。
     *
     * @param registry WebSocketTransportRegistration 注册对象
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 设置单个消息的最大大小（单位：字节），此处为10MB
        registry.setMessageSizeLimit(1024 * 1024 * 10);
        // 设置发送缓冲区的最大大小（单位：字节），此处为10MB
        registry.setSendBufferSizeLimit(1024 * 1024 * 10);
        // 设置首次消息的超时时间（单位：毫秒），此处为10秒
        registry.setTimeToFirstMessage(10000);
        // 设置发送消息的最大执行时长（单位：毫秒），此处为10秒
        registry.setSendTimeLimit(10000);

        // 添加自定义的WebSocketHandler修饰器工厂，用于增加连接日志、消息日志等
        registry.addDecoratorFactory(handler -> 
            new org.springframework.web.socket.handler.WebSocketHandlerDecorator(handler) {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    log.info("WebSocket connection established: {}", session.getId());
                    super.afterConnectionEstablished(session);
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus closeStatus) throws Exception {
                    log.info("WebSocket connection closed: {}, status: {}", session.getId(), closeStatus);
                    super.afterConnectionClosed(session, closeStatus);
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                    log.error("WebSocket transport error on session {}: {}", session.getId(), exception.getMessage(), exception);
                    super.handleTransportError(session, exception);
                }
            }
        );
    }

    /**
     * 配置客户端入站消息通道，可以添加拦截器实现认证等功能。
     *
     * @param registration 通道注册对象
     */
    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {
//        registration.executor();
//        registration.interceptors();
//        registration.taskExecutor();
//        registration.taskExecutor();
    }

    /**
     * 配置客户端出站消息通道，可以添加拦截器实现日志等功能。
     *
     * @param registration 通道注册对象
     */
    @Override
    public void configureClientOutboundChannel(
            ChannelRegistration registration) {
    }

    /**
     * 添加自定义参数解析器，用于方法调用时参数的自动绑定。
     *
     * @param argumentResolvers 解析器列表
     */
    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    /**
     * 添加自定义返回值处理器，用于处理方法返回值。
     *
     * @param returnValueHandlers 返回值处理器列表
     */
    @Override
    public void addReturnValueHandlers(
            List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

    /**
     * 配置消息转换器，消息的序列化与反序列化。
     *
     * @param messageConverters 消息转换器列表
     * @return 是否保留默认的消息转换器，true 保留
     */
    @Override
    public boolean configureMessageConverters(
            List<MessageConverter> messageConverters) {
        return true;
    }

    /**
     * 配置消息代理，定义消息目的地前缀、启用简单内存消息代理、用户前缀、发布顺序及缓存限制。
     *
     * @param registry 消息代理注册对象，用于设定 broker 的各项参数。
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 设置应用程序消息前缀，客户端发送消息到服务器时需要加上该前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 启用简单内存消息代理，仅支持 /queue 作为消息目的地前缀（点对点通信）
        registry.enableSimpleBroker("/queue");
        
        // 设置点对点消息的用户目标前缀（即用户发送和订阅的前缀）
        registry.setUserDestinationPrefix("/user");
        
        // 保证消息发布的顺序性
        registry.setPreservePublishOrder(true);
        
        // 设置简单 broker 的缓存消息限制数
        registry.setCacheLimit(1000);
    }

    /**
     * 返回此配置的排序顺序，默认为 null。
     *
     * @return 排序顺序
     */
    @org.springframework.lang.Nullable
    @Override
    public Integer getPhase() {
        return null;
    }
}
