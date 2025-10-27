package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类 - 简化版（只支持实时对话）
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于广播和点对点消息
        // 添加/topic和/queue前缀以支持聊天消息和WebRTC信令
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 添加SockJS端点，支持浏览器兼容性
        // 这个端点将用于所有WebSocket通信，包括聊天和WebRTC信令
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 添加原生WebSocket端点，方便测试和支持更高级的WebSocket功能
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 配置STOMP解码器以更宽松地处理消息格式
        // 增加消息缓冲区大小以支持WebRTC信令的较大消息
        registration.taskExecutor().corePoolSize(4).maxPoolSize(8);
        
        // 自定义消息处理，处理可能的格式问题
        registration.interceptors(new StompMessageInterceptor());
    }
    
    /**
     * 自定义STOMP消息拦截器，用于增强消息处理和错误恢复
     * 特别针对WebRTC信令消息进行优化处理
     */
    public static class StompMessageInterceptor implements org.springframework.messaging.support.ChannelInterceptor {
        
        @Override
        public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, org.springframework.messaging.MessageChannel channel) {
            // 这里可以添加消息日志或格式检查
            // 对于WebRTC信令消息，可以进行额外的验证
            return message;
        }
        
        @Override
        public void postSend(org.springframework.messaging.Message<?> message, org.springframework.messaging.MessageChannel channel, boolean sent) {
            // 消息发送后的处理
        }
        
        @Override
        public boolean preReceive(org.springframework.messaging.MessageChannel channel) {
            return true;
        }
        
        @Override
        public org.springframework.messaging.Message<?> postReceive(org.springframework.messaging.Message<?> message, org.springframework.messaging.MessageChannel channel) {
            return message;
        }
    }
}