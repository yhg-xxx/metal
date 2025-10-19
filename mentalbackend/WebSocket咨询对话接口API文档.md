# WebSocket 咨询对话接口API文档

## 1. 连接端点

### 1.1 SockJS端点（推荐用于浏览器兼容性）
```
/ws
```

### 1.2 原生WebSocket端点（用于测试或特殊需求）
```
/ws-native
```

## 2. 消息格式

### 2.1 发送消息格式

**目标路径**：`/app/chat.private`

**消息体**：
```json
{
  "senderId": 1,          // 发送者ID
  "receiverId": 2,        // 接收者ID
  "senderType": "USER",  // 发送者类型：USER 或 COUNSELOR
  "content": "消息内容"    // 消息内容
}
```

### 2.2 接收消息格式

**用户接收路径**：`/queue/messages/user/{userId}`

**咨询师接收路径**：`/queue/messages/counselor/{counselorId}`

**消息体**：
```json
{
  "senderId": 1,          // 发送者ID
  "receiverId": 2,        // 接收者ID
  "senderType": "USER",  // 发送者类型：USER 或 COUNSELOR
  "content": "消息内容",   // 消息内容
  "timestamp": "2023-01-01T12:00:00" // 消息时间戳
}
```

### 2.3 错误消息格式

**用户错误接收路径**：`/queue/errors/user/{userId}`

**咨询师错误接收路径**：`/queue/errors/counselor/{counselorId}`

**消息体**：
```json
{
  "message": "错误消息内容"
}
```

## 3. 功能说明

### 3.1 私聊功能
- 支持用户与咨询师之间的实时私聊
- 消息发送后，系统会同时发送给接收者和发送者（作为确认）
- 消息包含时间戳信息

### 3.2 错误处理
- 当处理消息过程中出现异常时，系统会发送错误消息给发送者

## 4. 连接配置

### 4.1 消息代理
- 启用了简单消息代理，支持 `/topic` 和 `/queue` 前缀的消息目的地
- 应用程序目的地前缀为 `/app`

### 4.2 跨域设置
- 允许所有来源的连接（`setAllowedOriginPatterns("*")`）

### 4.3 消息通道配置
- 客户端入站通道配置了线程池：核心线程数4，最大线程数8
- 包含自定义的STOMP消息拦截器用于消息处理和错误恢复