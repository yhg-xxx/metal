# WebRTC视频通话接口API文档

## 概述
本文档详细描述了基于WebRTC技术实现的点对点视频通话功能的后端接口规范。该功能允许用户和咨询师之间进行实时视频通话，通过WebSocket作为信令服务器来协调连接建立过程。

## 技术架构
- **前端**：WebRTC API (RTCPeerConnection, MediaStream等)
- **后端**：Spring Boot + WebSocket (STOMP协议) 作为信令服务器
- **数据流向**：
  1. 客户端通过WebSocket连接到后端服务器
  2. WebRTC信令消息(offer, answer, ICE候选)通过服务器转发
  3. 实际音视频流通过点对点P2P连接传输，不经过服务器

## 连接建立流程
1. 用户A发起视频通话请求
2. 服务器将通话请求转发给用户B
3. 用户A创建RTCPeerConnection并生成offer
4. Offer通过服务器转发给用户B
5. 用户B接受offer并创建answer
6. Answer通过服务器转发给用户A
7. 双方交换ICE候选信息
8. P2P连接建立，开始视频通话

## WebSocket连接端点

### 1. SockJS端点(兼容模式)
- **URL**: `/ws`
- **协议**: STOMP over SockJS
- **用途**: 用于浏览器兼容性较好的环境

### 2. 原生WebSocket端点
- **URL**: `/ws-native`
- **协议**: STOMP over WebSocket
- **用途**: 用于支持原生WebSocket的现代浏览器

## 消息格式与目的地

### WebRTC信令消息

#### 发送WebRTC信令
- **目的地**: `/app/webrtc.signal`
- **请求格式**:
  ```json
  {
    "senderId": 123,
    "receiverId": 456,
    "senderType": "USER", // 或 "COUNSELOR"
    "type": "offer", // 或 "answer", "ice-candidate"
    "data": "...", // SDP描述或ICE候选信息
    "callId": "unique-call-id-123"
  }
  ```

#### 接收WebRTC信令
- **目的地**: `/queue/webrtc/{userType}/{userId}`
  - 示例: `/queue/webrtc/user/123` 或 `/queue/webrtc/counselor/456`
- **响应格式**: 与发送格式相同

### WebRTC状态消息

#### 发送WebRTC状态
- **目的地**: `/app/webrtc.status`
- **请求格式**:
  ```json
  {
    "senderId": 123,
    "receiverId": 456,
    "senderType": "USER", // 或 "COUNSELOR"
    "status": "ringing", // 或 "accepted", "rejected", "ended"
    "callId": "unique-call-id-123"
  }
  ```

#### 接收WebRTC状态
- **目的地**: `/queue/webrtc/status/{userType}/{userId}`
  - 示例: `/queue/webrtc/status/user/123` 或 `/queue/webrtc/status/counselor/456`
- **响应格式**:
  ```json
  {
    "senderId": 123,
    "receiverId": 456,
    "senderType": "USER",
    "status": "ringing",
    "callId": "unique-call-id-123",
    "timestamp": "2024-01-01T12:00:00"
  }
  ```

## 错误处理

- **错误消息目的地**: `/queue/errors/{userType}/{userId}`
  - 示例: `/queue/errors/user/123`
- **错误消息格式**:
  ```json
  {
    "message": "错误描述"
  }
  ```

## 示例流程

### 1. 用户发起视频通话
```javascript
// 1. 发送通话请求状态
const callId = generateUniqueId();
const statusMessage = {
  senderId: currentUserId,
  receiverId: counselorId,
  senderType: "USER",
  status: "ringing",
  callId: callId
};

stompClient.send("/app/webrtc.status", {}, JSON.stringify(statusMessage));

// 2. 创建RTCPeerConnection并获取本地媒体流
const pc = new RTCPeerConnection(iceServers);
navigator.mediaDevices.getUserMedia({video: true, audio: true})
  .then(stream => {
    localVideo.srcObject = stream;
    stream.getTracks().forEach(track => pc.addTrack(track, stream));
    
    // 3. 创建offer
    return pc.createOffer();
  })
  .then(offer => pc.setLocalDescription(offer))
  .then(() => {
    // 4. 发送offer给咨询师
    const signalMessage = {
      senderId: currentUserId,
      receiverId: counselorId,
      senderType: "USER",
      type: "offer",
      data: JSON.stringify(pc.localDescription),
      callId: callId
    };
    
    stompClient.send("/app/webrtc.signal", {}, JSON.stringify(signalMessage));
  });
```

### 2. 咨询师接受视频通话
```javascript
// 1. 监听webrtc.status消息
stompClient.subscribe(`/queue/webrtc/status/counselor/${currentUserId}`, message => {
  const statusMsg = JSON.parse(message.body);
  
  if (statusMsg.status === "ringing") {
    // 显示来电通知
    showIncomingCallNotification(statusMsg);
    
    // 2. 当用户点击接受时
    acceptCallButton.onclick = () => {
      // 发送接受状态
      const acceptStatus = {
        senderId: currentUserId,
        receiverId: statusMsg.senderId,
        senderType: "COUNSELOR",
        status: "accepted",
        callId: statusMsg.callId
      };
      stompClient.send("/app/webrtc.status", {}, JSON.stringify(acceptStatus));
      
      // 创建RTCPeerConnection
      setupPeerConnection(statusMsg.callId, statusMsg.senderId);
    };
  }
});

// 3. 监听webrtc.signal消息
stompClient.subscribe(`/queue/webrtc/counselor/${currentUserId}`, message => {
  const signalMsg = JSON.parse(message.body);
  
  if (signalMsg.type === "offer") {
    // 处理offer并创建answer
    handleOffer(signalMsg);
  } else if (signalMsg.type === "ice-candidate") {
    // 添加ICE候选
    handleIceCandidate(signalMsg);
  }
});
```

## ICE服务器配置（前端需要）
为了提高NAT穿透成功率，建议配置STUN/TURN服务器：

```javascript
const iceServers = [
  {
    urls: 'stun:stun.l.google.com:19302'
  },
  // 可选：配置TURN服务器以应对复杂网络环境
  // {
  //   urls: 'turn:your-turn-server.com:3478',
  //   username: 'username',
  //   credential: 'credential'
  // }
];
```

## 注意事项
1. 确保在使用前获取用户的摄像头和麦克风权限
2. WebRTC需要在HTTPS环境下工作（本地开发可以使用http://localhost）
3. 对于复杂的网络环境，建议配置TURN服务器作为备用连接方式
4. 通话结束时记得关闭媒体流和释放资源
5. 信令服务器仅负责协调连接建立，不传输实际音视频数据

## 安全考虑
1. 实现适当的身份验证机制，确保只有合法用户能够建立连接
2. 考虑对信令消息进行加密
3. 实现通话请求验证，防止未授权的通话请求
4. 定期清理过期的通话会话