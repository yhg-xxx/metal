<script setup>
import { ref, onMounted, computed, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElSkeleton, ElMessageBox, ElAvatar } from 'element-plus';
import axios from 'axios';
import { websocketClient } from '@/websocket-client';
import {PhoneFilled, VideoCamera, ArrowLeft, More, Paperclip, Loading} from "@element-plus/icons-vue";

// 路由实例
const route = useRoute();
const router = useRouter();

// 状态变量
const messages = ref([]);
const loading = ref(false);
const newMessage = ref('');
const currentUserType = ref('COUNSELOR');
const isWebSocketConnected = ref(false);

// 联系人信息
const contactInfo = ref({
  nickname: '',
  avatar: '',
  id: ''
});

// WebRTC相关状态
const isVideoCallActive = ref(false);
const isCalling = ref(false);
const isReceivingCall = ref(false);
const remoteStream = ref(null);
const localStream = ref(null);
const peerConnection = ref(null);
const currentCallId = ref('');
const callStatus = ref('');

// 从路由参数获取ID
const userId = computed(() => route.query.userId || 26);
const counselorId = computed(() => route.query.counselorId || 11);

// 计算当前用户ID
const currentUserId = computed(() => {
  return currentUserType.value === 'COUNSELOR' ? counselorId.value : userId.value;
});

// 计算接收者ID
const receiverId = computed(() => {
  return currentUserType.value === 'COUNSELOR' ? userId.value : counselorId.value;
});

// 获取聊天记录
const fetchMessages = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/consultation/messages/conversation', {
      params: {
        userId: userId.value,
        counselorId: counselorId.value,
        limit: 50,
        offset: 0
      }
    });

    if (response.status === 200) {
      // 按时间排序，确保最新消息在底部
      messages.value = response.data.sort((a, b) => {
        const timeA = new Date(a.sentTime || a.timestamp).getTime();
        const timeB = new Date(b.sentTime || b.timestamp).getTime();
        return timeA - timeB;
      });
      // 滚动到底部
      await nextTick(() => {
        scrollToBottom();
      });
    } else {
      ElMessage.error('获取聊天记录失败');
    }
  } catch (error) {
    ElMessage.error('获取聊天记录失败：' + error.message);
    console.error('获取聊天记录失败:', error);
  } finally {
    loading.value = false;
  }
};

// 发送消息
const sendMessage = async () => {
  if (!newMessage.value.trim()) {
    return;
  }

  const messageDTO = {
    senderId: currentUserId.value,
    receiverId: receiverId.value,
    senderType: currentUserType.value,
    content: newMessage.value.trim()
  };

  try {
    websocketClient.sendMessage(messageDTO);

    // 本地立即显示消息（优化用户体验）
    const tempMessage = {
      id: Date.now(), // 临时ID
      content: newMessage.value.trim(),
      senderType: currentUserType.value,
      senderId: currentUserId.value,
      receiverId: receiverId.value,
      sentTime: new Date().toISOString(),
      isTemp: true // 标记为临时消息
    };

    messages.value.push(tempMessage);
    newMessage.value = '';

    // 滚动到底部
    await nextTick(() => {
      scrollToBottom();
    });

  } catch (error) {
    ElMessage.error('发送消息失败：' + error.message);
    console.error('发送消息失败:', error);
  }
};

// 接收消息处理
const handleIncomingMessage = (message) => {
  // 检查是否已存在相同消息（避免重复）
  const existingMessage = messages.value.find(
      msg => msg.id === message.id || (msg.isTemp && msg.content === message.content)
  );

  if (!existingMessage) {
    messages.value.push({
      ...message,
      sentTime: message.timestamp || new Date().toISOString()
    });

    // 滚动到底部
    nextTick(() => {
      scrollToBottom();
    });
  } else if (existingMessage.isTemp) {
    // 更新临时消息为正式消息
    const index = messages.value.findIndex(msg => msg.id === existingMessage.id);
    if (index !== -1) {
      messages.value[index] = {
        ...message,
        sentTime: message.timestamp || new Date().toISOString()
      };
    }
  }
};

// 初始化WebSocket连接
const initWebSocket = async () => {
  try {
    await websocketClient.connect(currentUserId.value, currentUserType.value);
    isWebSocketConnected.value = true;

    // 设置消息回调
    websocketClient.onMessageReceived(handleIncomingMessage);

    // 设置WebRTC信号回调
    websocketClient.onWebRTCSignal(handleWebRTCSignal);

    // 设置WebRTC状态回调
    websocketClient.onWebRTCStatus(handleWebRTCStatus);

    // 设置错误回调
    websocketClient.onError((error) => {
      ElMessage.error(error.message);
    });

    ElMessage.success('已连接到聊天服务器');
  } catch (error) {
    ElMessage.error('连接聊天服务器失败：' + error.message);
    console.error('WebSocket连接失败:', error);
  }
};

// WebRTC相关方法
const startVideoCall = async () => {
  try {
    isCalling.value = true;
    currentCallId.value = 'call_' + Date.now();

    // 初始化WebRTC连接
    await initWebRTC();

    // 发送呼叫请求
    const statusDTO = {
      senderId: currentUserId.value,
      receiverId: receiverId.value,
      senderType: currentUserType.value,
      status: 'ringing',
      callId: currentCallId.value
    };

    websocketClient.sendWebRTCStatus(statusDTO);
    callStatus.value = '等待对方接听...';

    ElMessage.info('正在呼叫对方...');

  } catch (error) {
    ElMessage.error('发起视频通话失败：' + error.message);
    isCalling.value = false;
    console.error('发起视频通话失败:', error);
  }
};

const initWebRTC = async () => {
  try {
    // 获取本地媒体流
    localStream.value = await navigator.mediaDevices.getUserMedia({
      video: true,
      audio: true
    });

    // 创建RTCPeerConnection
    const configuration = {
      iceServers: [
        { urls: 'stun:stun.l.google.com:19302' }
      ]
    };

    peerConnection.value = new RTCPeerConnection(configuration);

    // 添加本地流
    localStream.value.getTracks().forEach(track => {
      peerConnection.value.addTrack(track, localStream.value);
    });

    // ICE候选处理
    peerConnection.value.onicecandidate = (event) => {
      if (event.candidate) {
        const signalDTO = {
          senderId: currentUserId.value,
          receiverId: receiverId.value,
          senderType: currentUserType.value,
          type: 'ice-candidate',
          data: JSON.stringify(event.candidate),
          callId: currentCallId.value
        };
        websocketClient.sendWebRTCSignal(signalDTO);
      }
    };

    // 远程流处理
    peerConnection.value.ontrack = (event) => {
      remoteStream.value = event.streams[0];
      isVideoCallActive.value = true;
    };

    // 连接状态变化
    peerConnection.value.onconnectionstatechange = () => {
      console.log('连接状态:', peerConnection.value.connectionState);
    };

  } catch (error) {
    console.error('初始化WebRTC失败:', error);
    throw error;
  }
};

const handleWebRTCSignal = async (signal) => {
  if (!peerConnection.value) return;

  try {
    switch (signal.type) {
      case 'offer':
        // 设置远程描述
        await peerConnection.value.setRemoteDescription(
            new RTCSessionDescription(JSON.parse(signal.data))
        );

        // 创建应答
        const answer = await peerConnection.value.createAnswer();
        await peerConnection.value.setLocalDescription(answer);

        // 发送应答
        const answerSignal = {
          senderId: currentUserId.value,
          receiverId: signal.senderId,
          senderType: currentUserType.value,
          type: 'answer',
          data: JSON.stringify(answer),
          callId: signal.callId
        };
        websocketClient.sendWebRTCSignal(answerSignal);
        break;

      case 'answer':
        await peerConnection.value.setRemoteDescription(
            new RTCSessionDescription(JSON.parse(signal.data))
        );
        break;

      case 'ice-candidate':
        const candidate = JSON.parse(signal.data);
        await peerConnection.value.addIceCandidate(
            new RTCIceCandidate(candidate)
        );
        break;
    }
  } catch (error) {
    console.error('处理WebRTC信号失败:', error);
  }
};

const handleWebRTCStatus = async (status) => {
  switch (status.status) {
    case 'ringing':
      // 收到呼叫请求
      isReceivingCall.value = true;
      currentCallId.value = status.callId;

      // 显示接听/拒绝对话框
      ElMessageBox.confirm(
          `${status.senderType === 'USER' ? '用户' : '咨询师'} ${status.senderId} 请求视频通话`,
          '视频通话',
          {
            confirmButtonText: '接听',
            cancelButtonText: '拒绝',
            type: 'info'
          }
      ).then(async () => {
        // 接听
        isReceivingCall.value = false;
        await initWebRTC();

        // 发送接受状态
        const acceptStatus = {
          senderId: currentUserId.value,
          receiverId: status.senderId,
          senderType: currentUserType.value,
          status: 'accepted',
          callId: status.callId
        };
        websocketClient.sendWebRTCStatus(acceptStatus);

        callStatus.value = '通话中...';
        isVideoCallActive.value = true;

      }).catch(() => {
        // 拒绝
        isReceivingCall.value = false;
        const rejectStatus = {
          senderId: currentUserId.value,
          receiverId: status.senderId,
          senderType: currentUserType.value,
          status: 'rejected',
          callId: status.callId
        };
        websocketClient.sendWebRTCStatus(rejectStatus);
      });
      break;

    case 'accepted':
      isCalling.value = false;
      callStatus.value = '通话中...';

      // 创建offer
      const offer = await peerConnection.value.createOffer();
      await peerConnection.value.setLocalDescription(offer);

      // 发送offer
      const offerSignal = {
        senderId: currentUserId.value,
        receiverId: status.senderId,
        senderType: currentUserType.value,
        type: 'offer',
        data: JSON.stringify(offer),
        callId: status.callId
      };
      websocketClient.sendWebRTCSignal(offerSignal);
      break;

    case 'rejected':
      isCalling.value = false;
      ElMessage.warning('对方拒绝了视频通话');
      endVideoCall();
      break;

    case 'ended':
      isVideoCallActive.value = false;
      isCalling.value = false;
      callStatus.value = '';
      ElMessage.info('通话已结束');
      endVideoCall();
      break;
  }
};

const endVideoCall = () => {
  // 发送结束状态
  if (currentCallId.value) {
    const endStatus = {
      senderId: currentUserId.value,
      receiverId: receiverId.value,
      senderType: currentUserType.value,
      status: 'ended',
      callId: currentCallId.value
    };
    websocketClient.sendWebRTCStatus(endStatus);
  }

  // 清理资源
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop());
    localStream.value = null;
  }

  if (peerConnection.value) {
    peerConnection.value.close();
    peerConnection.value = null;
  }

  isVideoCallActive.value = false;
  isCalling.value = false;
  isReceivingCall.value = false;
  remoteStream.value = null;
  currentCallId.value = '';
  callStatus.value = '';
};

// 工具函数
const formatTime = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

const isCurrentUser = (senderType) => {
  return senderType === currentUserType.value;
};

const scrollToBottom = () => {
  const messagesContainer = document.querySelector('.chat-messages');
  if (messagesContainer) {
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }
};

// 键盘事件处理
const handleKeyPress = (event) => {
  if (event.ctrlKey && event.key === 'Enter') {
    sendMessage();
  }
};

// 不再需要返回按钮，因为现在是作为子组件嵌入在左右布局中

// 生命周期
onMounted(async () => {
  // 获取联系人信息
  contactInfo.value.nickname = decodeURIComponent(route.query.nickname || '');
  contactInfo.value.avatar = decodeURIComponent(route.query.avatar || '');
  contactInfo.value.id = route.query.userId;
  
  await fetchMessages();
  await initWebSocket();

  // 添加键盘事件监听
  window.addEventListener('keydown', handleKeyPress);
});

onUnmounted(() => {
  // 清理WebSocket连接
  websocketClient.disconnect();

  // 结束视频通话
  endVideoCall();

  // 移除键盘事件监听
  window.removeEventListener('keydown', handleKeyPress);
});

// 监听路由参数变化，切换聊天对象时重新获取数据
watch(() => [route.query.userId, route.query.nickname, route.query.avatar], async () => {
  // 获取联系人信息
  contactInfo.value.nickname = decodeURIComponent(route.query.nickname || '');
  contactInfo.value.avatar = decodeURIComponent(route.query.avatar || '');
  contactInfo.value.id = route.query.userId;
  
  // 清空当前消息列表
  messages.value = [];
  
  // 先断开现有WebSocket连接
  websocketClient.disconnect();
  
  // 重新获取聊天记录
  await fetchMessages();
  
  // 重新初始化WebSocket连接
  await initWebSocket();
}, { deep: true });
</script>

<template>
  <div class="chat-detail-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-left">
        <el-avatar :size="40" :src="contactInfo.avatar">{{ contactInfo.nickname.charAt(0).toUpperCase() }}</el-avatar>
        <div class="contact-info">
          <h3 class="nickname">{{ contactInfo.nickname || '用户' }}</h3>
          <span class="status">在线</span>
        </div>
      </div>
      <div class="header-right">
        <el-button type="text" @click="startVideoCall" :disabled="isCalling || isVideoCallActive" :loading="isCalling">
          <el-icon><VideoCamera /></el-icon>
        </el-button>
        <el-button type="text">
          <el-icon><More /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 视频通话区域 -->
    <div v-if="isVideoCallActive" class="video-call-container">
      <div class="video-grid">
        <div class="video-item local-video">
          <h4>本地视频</h4>
          <video
              v-if="localStream"
              :srcObject="localStream"
              autoplay=""
              muted=""
              playsinline
          ></video>
        </div>
        <div class="video-item remote-video">
          <h4>对方视频</h4>
          <video
              v-if="remoteStream"
              :srcObject="remoteStream"
              autoplay=""
              playsinline
          ></video>
        </div>
      </div>
      <div class="call-controls">
        <el-button type="danger" @click="endVideoCall">
          <el-icon><PhoneFilled /></el-icon>
          结束通话
        </el-button>
      </div>
    </div>

    <!-- 聊天记录区域 -->
    <div class="chat-messages">
      <!-- 加载骨架屏 -->
      <div v-if="loading" class="loading-skeleton">
        <ElSkeleton :rows="10" animated />
      </div>

      <!-- 聊天记录列表 -->
      <div v-else-if="messages.length > 0" class="messages-list">
        <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{
            'current-user': isCurrentUser(message.senderType),
            'temp-message': message.isTemp
          }"
        >
          <!-- 对方用户头像 -->
          <el-avatar
              v-if="!isCurrentUser(message.senderType)"
              :size="36"
              :src="contactInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
              class="avatar"
          >
            {{ contactInfo.nickname.charAt(0).toUpperCase() }}
          </el-avatar>
          
          <div class="message-wrapper">
            <div class="message-content">
              {{ message.content }}
              <span v-if="message.isTemp" class="sending-indicator">
                <el-icon class="is-loading"><Loading /></el-icon>
              </span>
            </div>
            <span class="time">{{ formatTime(message.sentTime) }}</span>
          </div>
          
          <!-- 当前用户头像 -->
          <el-avatar
              v-if="isCurrentUser(message.senderType)"
              :size="36"
              :src="'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
              class="avatar"
          >
            {{ currentUserNickname.charAt(0).toUpperCase() }}
          </el-avatar>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-messages">
        <el-empty description="暂无聊天记录" />
      </div>
    </div>

    <!-- 消息输入区域 -->
    <div class="chat-input-area">
      <div class="input-wrapper">
        <div class="input-tools">
          <el-button type="text">
            <el-icon><Paperclip /></el-icon>
          </el-button>
          <el-button type="text">
            <el-icon><Paperclip /></el-icon>
          </el-button>
        </div>
        <div class="input-area">
          <el-input
              v-model="newMessage"
              type="textarea"
              placeholder="输入消息..."
              :rows="1"
              maxlength="500"
              show-word-limit
              :disabled="!isWebSocketConnected"
          />
          <el-button
              type="primary"
              @click="sendMessage"
              :disabled="!newMessage.trim() || !isWebSocketConnected"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-detail-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f9f9f9;
  overflow: hidden;
}

/* 聊天头部样式 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .el-button {
      color: #333;

      .el-icon {
        font-size: 20px;
      }
    }

    .el-avatar {
      border-radius: 50%;
      cursor: pointer;
    }

    .contact-info {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .nickname {
        font-size: 16px;
        font-weight: 500;
        margin: 0;
      }

      .status {
        font-size: 12px;
        color: #67c23a;
      }
    }
  }

  .header-right {
    display: flex;
    gap: 8px;

    .el-button {
      color: #333;

      .el-icon {
        font-size: 22px;
      }
    }
  }
}

/* 视频通话容器 */
.video-call-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #000;
  z-index: 1000;
  display: flex;
  flex-direction: column;

  .video-grid {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 10px;
    padding: 20px;
    justify-items: center;
    align-items: center;

    video {
      width: 100%;
      max-width: 500px;
      border-radius: 8px;
      background-color: #333;
    }

    .local-video {
      position: absolute;
      top: 20px;
      right: 20px;
      width: 150px;
      max-width: none;
      border-radius: 8px;
      border: 2px solid #fff;
      z-index: 10;
    }
  }

  .call-controls {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
    padding: 20px;
    background-color: rgba(0, 0, 0, 0.7);

    button {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 24px;
      border: none;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: scale(1.1);
      }

      &.hangup-btn {
        background-color: #ff4d4f;
        color: #fff;
      }

      &.mute-btn {
        background-color: rgba(255, 255, 255, 0.2);
        color: #fff;
      }
    }
  }
}

/* 聊天消息容器 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-image: url('https://res.wx.qq.com/a/wx_fed/weixin_portal/res/static/img/202308/9d51630e11986120e9e5f40b9b7852cb.jpg');
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;

  /* 滚动条样式 */
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.05);
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.3);
  }
}

/* 消息项 */
.message-item {
  display: flex;
  margin-bottom: 12px;
  align-items: flex-end;
  gap: 10px;

  /* 发送的消息 */
  &.current-user {
    flex-direction: row-reverse;
    justify-content: flex-start;

    .message-wrapper {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }

    .message-content {
      background-color: #95ec69;
      border-radius: 18px 18px 4px 18px;
      color: #333;
    }
  }

  /* 接收的消息 */
  &:not(.current-user) {
    justify-content: flex-start;

    .message-wrapper {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
    }

    .message-content {
      background-color: #fff;
      border-radius: 18px 18px 18px 4px;
      color: #333;
    }
  }

  .avatar {
    flex-shrink: 0;
  }

  .message-wrapper {
    max-width: 70%;
  }

  .message-content {
    padding: 10px 14px;
    line-height: 1.4;
    word-break: break-word;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    max-width: 100%;
  }

  .time {
    font-size: 11px;
    color: #999;
    margin: 4px 0;
    text-align: left;
    padding: 0 8px;
  }

  .current-user .time {
    text-align: right;
  }

  .sending-indicator {
    display: inline-block;
    margin-left: 4px;
    font-size: 14px;
    color: #999;
  }
}

/* 消息骨架屏 */
.loading-skeleton {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;

  .skeleton-content {
    width: 200px;
    height: 40px;
    border-radius: 18px;
  }
}

/* 消息输入区域样式 */
.chat-input-area {
  padding: 12px 16px;
  background-color: #f5f5f5;
  border-top: 1px solid #e0e0e0;

  .input-wrapper {
    position: relative;

    .el-input {
      background-color: #fff;
      border-radius: 20px;
      padding: 8px 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

      .el-textarea__inner {
        border: none;
        resize: none;
        padding: 8px 0;
        min-height: 40px;
        font-size: 14px;
        line-height: 1.4;

        &:focus {
          outline: none;
          box-shadow: none;
        }
      }
    }

    .input-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;

      .hint {
        font-size: 12px;
        color: #909399;
      }

      .el-button {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        padding: 0;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #07c160;
        color: #fff;
        border: none;

        &:hover {
          background-color: #06ad56;
        }

        .el-icon {
          font-size: 20px;
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-detail-container {
    height: 100vh;
  }

  .chat-header {
    padding: 8px 12px;

    .header-left {
      gap: 8px;

      .el-avatar {
        size: 32px;
      }

      .contact-info {
        .nickname {
          font-size: 14px;
        }
      }
    }
  }

  .chat-messages {
    padding: 12px;

    .message-item {
      &.current-user {
        margin-left: 10%;
      }

      &:not(.current-user) {
        margin-right: 10%;
      }

      .message-content {
        max-width: 90%;
        padding: 8px 12px;
      }
    }
  }

  .chat-input-area {
    padding: 8px 12px;

    .input-tools {
      gap: 12px;

      .el-button {
        .el-icon {
          font-size: 18px;
        }
      }
    }

    .input-area {
      gap: 8px;

      .el-input {
        .el-textarea__inner {
          font-size: 14px;
        }
      }

      .el-button {
        width: 36px;
        height: 36px;

        .el-icon {
          font-size: 18px;
        }
      }
    }
  }
}
</style>