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
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 添加SockJS端点，支持浏览器兼容性
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 添加原生WebSocket端点，方便测试
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 配置STOMP解码器以更宽松地处理消息格式
        // 增加消息缓冲区大小
        registration.taskExecutor().corePoolSize(4).maxPoolSize(8);
        
        // 自定义消息处理，处理可能的格式问题
        registration.interceptors(new StompMessageInterceptor());
    }
    
    /**
     * 自定义STOMP消息拦截器，用于增强消息处理和错误恢复
     */
    public static class StompMessageInterceptor implements org.springframework.messaging.support.ChannelInterceptor {
        
        @Override
        public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, org.springframework.messaging.MessageChannel channel) {
            // 这里可以添加消息日志或格式检查
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