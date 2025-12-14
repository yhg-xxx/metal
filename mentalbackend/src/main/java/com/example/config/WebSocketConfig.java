package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket配置类 - 优化版（支持实时对话和视频通话）
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置线程池任务调度器Bean
     * 用于WebSocket心跳机制和消息代理定时任务
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("STOMP-HEARTBEAT-");
        taskScheduler.initialize(); // 显式初始化
        return taskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于广播和点对点消息
        // 添加/topic和/queue前缀以支持聊天消息和WebRTC信令
        // 添加心跳机制配置
        config.enableSimpleBroker("/topic", "/queue")
                // 添加心跳机制，每25秒发送一次心跳，60秒内无响应则断开连接
                .setHeartbeatValue(new long[]{25000, 60000})
                .setTaskScheduler(taskScheduler());
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀，用于点对点通信
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 添加SockJS端点，支持浏览器兼容性
        // 这个端点将用于所有WebSocket通信，包括聊天和WebRTC信令
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(10000)
                .setSessionCookieNeeded(true)
                // 设置SockJS客户端断开连接后的最大等待时间为60秒
                .setDisconnectDelay(60000);

        // 添加原生WebSocket端点，方便测试和支持更高级的WebSocket功能
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 配置WebSocket传输，增加消息缓冲区大小以支持WebRTC信令的较大消息
        registration.setMessageSizeLimit(200 * 1024); // 增加消息大小限制到200KB
        registration.setSendBufferSizeLimit(1024 * 1024); // 设置发送缓冲区大小
        registration.setSendTimeLimit(20000); // 设置发送超时时间
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 配置入站通道的线程池大小，提高并发处理能力
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(10)
                .queueCapacity(500);
        
        // 自定义消息处理，处理可能的格式问题
        registration.interceptors(new StompMessageInterceptor());
    }
    
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 配置出站通道的线程池大小，提高并发处理能力
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(10)
                .queueCapacity(500);
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