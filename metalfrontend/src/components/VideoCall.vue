<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// 使用浏览器原生WebSocket API，不使用sockjs-client以避免兼容性问题
import { Client } from '@stomp/stompjs';

// 添加全局变量定义，解决可能的兼容性问题
if (typeof window !== 'undefined' && !window.global) {
  window.global = window;
}

// 组件状态
const isConnected = ref(false);
const isInCall = ref(false);
const isConnecting = ref(false);
const isRinging = ref(false);
const localStream = ref(null);
const remoteStream = ref(null);
const peerConnection = ref(null);
const stompClient = ref(null);
const currentCallId = ref(null);
const callerId = ref(null);
const callerType = ref(null);
const localVideoRef = ref(null);
const remoteVideoRef = ref(null);

// 用户信息（从localStorage获取）
const userType = ref('COUNSELOR'); // USER 或 COUNSELOR
const userId = ref(null);
const counselor = ref(null);

// 从localStorage获取用户信息
const getCounselorInfo = () => {
  try {
    const storedCounselor = localStorage.getItem('counselor');
    if (storedCounselor) {
      counselor.value = JSON.parse(storedCounselor);
      userId.value = counselor.value.counselorId;
      userType.value = 'COUNSELOR';
      console.log('从localStorage获取到用户信息:', { userId: userId.value, counselor: counselor.value });
    } else {
      console.warn('localStorage中未找到用户信息');
    }
  } catch (error) {
    console.error('解析用户信息失败:', error);
  }
};

// 生成唯一的callId
const generateCallId = () => {
  return 'call_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
};

// 连接WebSocket服务器
const connectWebSocket = () => {
  if (isConnecting.value || isConnected.value) return;

  isConnecting.value = true;

  try {
    // 使用浏览器原生WebSocket和@stomp/stompjs
    const socket = new WebSocket('ws://localhost:8085/ws-native');
    console.log('正在连接WebSocket服务器...');
    console.log('当前用户ID:', userId.value, '用户类型:', userType.value);

    // 创建STOMP客户端
    stompClient.value = new Client({
      webSocketFactory: () => socket,
      onConnect: (frame) => {
        console.log('WebSocket连接成功:', frame);
        isConnected.value = true;
        isConnecting.value = false;

        // 订阅WebRTC信令消息 - 修复订阅路径
        const userTypeLower = userType.value.toLowerCase();
        // 使用counselorId作为WebSocket用户ID
        const wsUserId = userId.value;
        console.log('开始订阅消息，路径前缀:', userTypeLower, '用户ID:', wsUserId);

        // 订阅WebRTC信令消息
        const signalSubscription = stompClient.value.subscribe(
            `/queue/webrtc/${userTypeLower}/${wsUserId}`,
            (message) => {
              console.log('收到WebRTC信令消息:', message);
              handleSignalMessage(message);
            }
        );

        // 订阅WebRTC状态消息
        const statusSubscription = stompClient.value.subscribe(
            `/queue/webrtc/status/${userTypeLower}/${wsUserId}`,
            (message) => {
              console.log('收到WebRTC状态消息:', message);
              handleStatusMessage(message);
            }
        );

        // 订阅错误消息
        const errorSubscription = stompClient.value.subscribe(
            `/queue/errors/${userTypeLower}/${wsUserId}`,
            (message) => handleErrorMessage(message)
        );

        console.log('订阅完成');
        ElMessage.success('WebSocket连接成功');
      },
      onDisconnect: () => {
        isConnected.value = false;
        ElMessage.warning('WebSocket连接已断开');
      },
      onStompError: (error) => {
        console.error('WebSocket连接错误:', error);
        isConnecting.value = false;
        ElMessage.error('WebSocket连接失败，请检查网络或服务器状态');
      },
      connectHeaders: {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    // 启动连接
    stompClient.value.activate();

  } catch (error) {
    console.error('WebSocket初始化失败:', error);
    isConnecting.value = false;
    ElMessage.error('WebSocket初始化失败');
  }
};

// 断开WebSocket连接
const disconnectWebSocket = () => {
  if (stompClient.value) {
    try {
      stompClient.value.deactivate();
      isConnected.value = false;
      ElMessage.info('WebSocket连接已断开');
    } catch (error) {
      console.error('断开WebSocket连接失败:', error);
    }
  }
};

// 获取本地媒体流
const getLocalStream = async () => {
  try {
    // 请求摄像头和麦克风权限
    const stream = await navigator.mediaDevices.getUserMedia({
      video: true,
      audio: true
    });
    localStream.value = stream;

    // 将流分配给本地视频元素
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = stream;
    }

    return stream;
  } catch (error) {
    console.error('获取本地媒体流失败:', error);
    ElMessage.error('无法访问摄像头或麦克风，请检查权限设置');
    throw error;
  }
};

// 停止并释放媒体流
const stopLocalStream = () => {
  if (localStream.value) {
    localStream.value.getTracks().forEach(track => track.stop());
    localStream.value = null;
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = null;
    }
  }

  if (remoteStream.value) {
    remoteStream.value.getTracks().forEach(track => track.stop());
    remoteStream.value = null;
    if (remoteVideoRef.value) {
      remoteVideoRef.value.srcObject = null;
    }
  }
};

// 创建PeerConnection
const createPeerConnection = (callId, remoteUserId, remoteUserType) => {
  // 关闭之前的连接
  if (peerConnection.value) {
    try {
      peerConnection.value.close();
    } catch (e) {
      console.warn('关闭之前的PeerConnection时出错:', e);
    }
    peerConnection.value = null;
  }

  // 创建新的PeerConnection - 简化配置
  const configuration = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' }
    ],
    iceTransportPolicy: 'all'
  };

  try {
    peerConnection.value = new RTCPeerConnection(configuration);
    currentCallId.value = callId;

    // 监听ICE候选事件
    peerConnection.value.onicecandidate = (event) => {
      if (event.candidate) {
        console.log('生成ICE候选:', event.candidate);
        // 发送ICE候选到对方
        sendSignal(remoteUserId, remoteUserType, 'ice-candidate', {
          sdpMid: event.candidate.sdpMid,
          sdpMLineIndex: event.candidate.sdpMLineIndex,
          candidate: event.candidate.candidate
        });
      } else {
        console.log('ICE候选收集完成');
      }
    };

    // 处理远程流
    peerConnection.value.ontrack = (event) => {
      console.log('收到远程轨道事件:', event);
      if (event.streams && event.streams.length > 0) {
        remoteStream.value = event.streams[0];
        if (remoteVideoRef.value) {
          remoteVideoRef.value.srcObject = remoteStream.value;
          console.log('远程视频流已设置，轨道数量:', event.streams[0].getTracks().length);

          // 检查视频是否开始播放
          setTimeout(() => {
            if (remoteVideoRef.value.readyState === 4) {
              console.log('远程视频正在播放');
            } else {
              console.log('远程视频状态:', remoteVideoRef.value.readyState);
            }
          }, 1000);
        }
      }
    };

    // 连接状态监控
    peerConnection.value.onconnectionstatechange = () => {
      const state = peerConnection.value.connectionState;
      console.log('WebRTC连接状态:', state);

      if (state === 'connected') {
        ElMessage.success('视频通话连接已建立');
        isInCall.value = true;
      } else if (state === 'failed') {
        console.error('WebRTC连接失败');
        ElMessage.error('视频连接失败');
        handleCallEnded();
      } else if (state === 'disconnected') {
        console.warn('WebRTC连接断开');
        ElMessage.warning('视频连接断开');
      }
    };

    // ICE连接状态监控
    peerConnection.value.oniceconnectionstatechange = () => {
      console.log('ICE连接状态:', peerConnection.value.iceConnectionState);
    };

    // 添加本地流到连接
    if (localStream.value) {
      localStream.value.getTracks().forEach(track => {
        console.log('添加本地轨道:', track.kind, track.id);
        try {
          peerConnection.value.addTrack(track, localStream.value);
        } catch (addError) {
          console.error('添加轨道失败:', addError);
        }
      });
      console.log('本地媒体轨道已添加到PeerConnection');
    } else {
      console.warn('本地流未就绪，无法添加到PeerConnection');
    }

    console.log('PeerConnection创建成功');
  } catch (error) {
    console.error('创建PeerConnection失败:', error);
    throw error;
  }
};

// 发送WebRTC信令消息
const sendSignal = (receiverId, receiverType, type, data) => {
  if (!stompClient.value || !stompClient.value.connected) {
    ElMessage.error('WebSocket未连接');
    return;
  }

  // 确保callId存在
  if (!currentCallId.value) {
    currentCallId.value = generateCallId();
  }

  // 使用counselorId作为发送者ID
  const actualSenderId = userId.value;

  const signalMessage = {
    senderId: actualSenderId,
    receiverId: receiverId,
    senderType: userType.value,
    type: type,
    data: typeof data === 'string' ? data : JSON.stringify(data),
    callId: currentCallId.value
  };

  console.log('发送信令消息:', signalMessage);

  try {
    stompClient.value.publish({
      destination: '/app/webrtc.signal',
      body: JSON.stringify(signalMessage)
    });
  } catch (error) {
    console.error('发送信令消息失败:', error);
    ElMessage.error('发送消息失败');
  }
};

// 发送WebRTC状态消息
const sendStatus = (receiverId, receiverType, status) => {
  if (!stompClient.value || !stompClient.value.connected) {
    ElMessage.error('WebSocket未连接');
    return;
  }

  // 确保callId存在
  if (!currentCallId.value) {
    currentCallId.value = generateCallId();
  }

  // 使用counselorId作为发送者ID
  const actualSenderId = userId.value;

  const statusMessage = {
    senderId: actualSenderId,
    receiverId: receiverId,
    senderType: userType.value,
    status: status,
    callId: currentCallId.value
  };

  console.log('发送状态消息:', statusMessage);

  try {
    stompClient.value.publish({
      destination: '/app/webrtc.status',
      body: JSON.stringify(statusMessage)
    });
  } catch (error) {
    console.error('发送状态消息失败:', error);
    ElMessage.error('发送状态消息失败');
  }
};

// 处理收到的信令消息
const handleSignalMessage = (message) => {
  try {
    const signalMsg = JSON.parse(message.body);
    console.log('处理信令消息:', signalMsg.type, '完整数据:', signalMsg);

    // 确保callId存在
    if (signalMsg.callId) {
      currentCallId.value = signalMsg.callId;
    }

    switch (signalMsg.type) {
      case 'offer':
        console.log('处理offer消息');
        handleOffer(signalMsg);
        break;
      case 'answer':
        console.log('处理answer消息');
        handleAnswer(signalMsg);
        break;
      case 'ice-candidate':
        console.log('处理ice-candidate消息');
        handleIceCandidate(signalMsg);
        break;
      default:
        console.warn('未知的信令类型:', signalMsg.type);
    }
  } catch (error) {
    console.error('处理信令消息失败:', error, '原始消息:', message);
    ElMessage.error('处理通话信令失败');
  }
};

// 处理收到的状态消息
const handleStatusMessage = (message) => {
  try {
    const statusMsg = JSON.parse(message.body);
    console.log('处理状态消息:', statusMsg.status, '完整数据:', statusMsg);

    // 确保callId存在
    if (statusMsg.callId) {
      currentCallId.value = statusMsg.callId;
    }

    switch (statusMsg.status) {
      case 'ringing':
        if (!isInCall.value && !isRinging.value) {
          console.log('收到来电请求');
          handleIncomingCall(statusMsg);
        }
        break;
      case 'request':
        if (!isInCall.value && !isRinging.value) {
          console.log('收到通话请求');
          handleIncomingCall(statusMsg);
        }
        break;
      case 'accepted':
        console.log('对方已接受通话');
        ElMessage.success('对方已接受通话');
        break;
      case 'rejected':
        console.log('对方拒绝了通话');
        ElMessage.info('对方拒绝了通话');
        handleCallEnded();
        break;
      case 'ended':
        console.log('对方已结束通话');
        ElMessage.info('对方已结束通话');
        handleCallEnded();
        break;
      default:
        console.log('未处理的状态消息:', statusMsg.status);
    }
  } catch (error) {
    console.error('处理状态消息失败:', error, '原始消息:', message);
    ElMessage.error('处理通话状态失败');
  }
};

// 处理错误消息
const handleErrorMessage = (message) => {
  try {
    const errorMsg = JSON.parse(message.body);
    console.error('收到错误消息:', errorMsg);
    ElMessage.error(errorMsg.message || '发生错误');
  } catch (error) {
    console.error('处理错误消息失败:', error);
  }
};

// 处理来电
const handleIncomingCall = (statusMsg) => {
  isRinging.value = true;
  callerId.value = statusMsg.senderId;
  callerType.value = statusMsg.senderType;

  console.log('收到来电:', { callerId: statusMsg.senderId, callerType: statusMsg.senderType, callId: statusMsg.callId });

  // 显示来电对话框
  ElMessageBox.confirm(
      `收到来自 ${statusMsg.senderType === 'USER' ? '用户' : '咨询师'} ${statusMsg.senderId} 的视频通话请求，是否接受？`,
      '来电通知',
      {
        confirmButtonText: '接受',
        cancelButtonText: '拒绝',
        type: 'warning',
        showClose: false,
        closeOnClickModal: false,
        closeOnPressEscape: false
      }
  )
      .then(async () => {
        // 接受通话
        isRinging.value = false;
        isInCall.value = true;

        try {
          // 获取本地媒体流
          await getLocalStream();

          // 使用状态消息中的senderId作为目标ID
          const targetId = statusMsg.senderId;

          // 创建PeerConnection
          createPeerConnection(statusMsg.callId, targetId, statusMsg.senderType);

          // 发送接受状态
          sendStatus(targetId, statusMsg.senderType, 'accepted');

          ElMessage.success('通话已接受，正在建立连接...');
        } catch (error) {
          console.error('接受通话失败:', error);
          ElMessage.error('接受通话失败: ' + error.message);
          handleCallEnded();
        }
      })
      .catch(() => {
        // 拒绝通话
        isRinging.value = false;
        sendStatus(statusMsg.senderId, statusMsg.senderType, 'rejected');
        ElMessage.info('已拒绝通话');
      });
};

// 处理offer
const handleOffer = async (signalMsg) => {
  console.log('开始处理offer，发送者:', signalMsg.senderId, 'callId:', signalMsg.callId);

  try {
    // 如果还没有PeerConnection，创建它
    if (!peerConnection.value) {
      await getLocalStream();
      createPeerConnection(signalMsg.callId, signalMsg.senderId, signalMsg.senderType);
    }

    // 解析offer数据 - 修复解析逻辑
    let offerData;
    try {
      if (typeof signalMsg.data === 'string') {
        const parsed = JSON.parse(signalMsg.data);
        // 检查是否是WebRTC的SDP对象
        if (parsed.sdp && parsed.type) {
          offerData = parsed;
        } else if (parsed.sdp) {
          // 如果只有sdp字段
          offerData = {
            type: 'offer',
            sdp: parsed.sdp
          };
        } else {
          // 直接当作sdp字符串处理
          offerData = {
            type: 'offer',
            sdp: parsed
          };
        }
      } else if (signalMsg.data.sdp) {
        // 如果data已经是对象且有sdp
        offerData = signalMsg.data;
      } else {
        console.error('无法解析的offer数据格式:', signalMsg.data);
        throw new Error('无效的offer数据格式');
      }
    } catch (parseError) {
      console.error('解析offer数据失败:', parseError);
      // 尝试直接作为SDP字符串处理
      offerData = {
        type: 'offer',
        sdp: signalMsg.data
      };
    }

    console.log('设置远程描述(offer):', offerData);

    // 设置远程描述
    const remoteDesc = new RTCSessionDescription({
      type: 'offer',
      sdp: offerData.sdp || offerData
    });

    await peerConnection.value.setRemoteDescription(remoteDesc);

    // 创建answer
    const answer = await peerConnection.value.createAnswer({
      offerToReceiveAudio: true,
      offerToReceiveVideo: true
    });

    console.log('创建的answer:', answer);

    // 设置本地描述
    await peerConnection.value.setLocalDescription(answer);

    // 发送answer - 修复发送格式
    const answerToSend = {
      type: answer.type,
      sdp: answer.sdp
    };

    sendSignal(
        signalMsg.senderId,
        signalMsg.senderType,
        'answer',
        JSON.stringify(answerToSend)
    );

    console.log('Answer发送完成');

  } catch (error) {
    console.error('处理offer失败:', error);
    ElMessage.error('处理通话请求失败: ' + error.message);
  }
};

// 处理answer
// 修复handleAnswer方法
const handleAnswer = async (signalMsg) => {
  console.log('开始处理answer，发送者:', signalMsg.senderId, 'callId:', signalMsg.callId);

  if (!peerConnection.value) {
    console.error('PeerConnection未初始化');
    ElMessage.error('PeerConnection未初始化');
    return;
  }

  try {
    // 解析answer数据 - 修复解析逻辑
    let answerData;
    try {
      if (typeof signalMsg.data === 'string') {
        const parsed = JSON.parse(signalMsg.data);
        if (parsed.sdp && parsed.type) {
          answerData = parsed;
        } else if (parsed.sdp) {
          answerData = {
            type: 'answer',
            sdp: parsed.sdp
          };
        } else {
          answerData = {
            type: 'answer',
            sdp: parsed
          };
        }
      } else if (signalMsg.data.sdp) {
        answerData = signalMsg.data;
      } else {
        console.error('无法解析的answer数据格式:', signalMsg.data);
        throw new Error('无效的answer数据格式');
      }
    } catch (parseError) {
      console.error('解析answer数据失败:', parseError);
      answerData = {
        type: 'answer',
        sdp: signalMsg.data
      };
    }

    console.log('设置远程描述(answer):', answerData);

    // 检查当前状态
    if (peerConnection.value.signalingState === 'stable') {
      console.log('当前信令状态稳定，可以设置远程描述');
    } else {
      console.log('当前信令状态:', peerConnection.value.signalingState);
    }

    const remoteDesc = new RTCSessionDescription({
      type: 'answer',
      sdp: answerData.sdp || answerData
    });

    await peerConnection.value.setRemoteDescription(remoteDesc);

    console.log('远程描述设置成功，当前信令状态:', peerConnection.value.signalingState);

  } catch (error) {
    console.error('处理answer失败:', error);
    ElMessage.error('建立通话连接失败: ' + error.message);

    // 尝试恢复
    if (error.message.includes('already')) {
      console.log('尝试恢复连接...');
      try {
        // 重新创建PeerConnection
        const currentRemote = peerConnection.value.remoteDescription;
        if (currentRemote) {
          await peerConnection.value.setRemoteDescription(currentRemote);
          console.log('重新设置远程描述成功');
        }
      } catch (recoverError) {
        console.error('恢复连接失败:', recoverError);
      }
    }
  }
};

// 处理ICE候选
const handleIceCandidate = async (signalMsg) => {
  console.log('处理ICE候选，发送者:', signalMsg.senderId);

  if (!peerConnection.value) {
    console.log('PeerConnection未就绪，等待中...');
    // 可以缓存ICE候选，等待PeerConnection就绪
    setTimeout(() => {
      if (peerConnection.value && peerConnection.value.remoteDescription) {
        handleIceCandidate(signalMsg);
      }
    }, 1000);
    return;
  }

  // 检查是否已经有远程描述
  if (!peerConnection.value.remoteDescription) {
    console.log('等待远程描述设置...');
    setTimeout(() => handleIceCandidate(signalMsg), 500);
    return;
  }

  try {
    let candidateData;
    if (typeof signalMsg.data === 'string') {
      candidateData = JSON.parse(signalMsg.data);
    } else {
      candidateData = signalMsg.data;
    }

    console.log('添加ICE候选:', candidateData);

    if (candidateData && candidateData.candidate) {
      const iceCandidate = new RTCIceCandidate({
        sdpMid: candidateData.sdpMid,
        sdpMLineIndex: candidateData.sdpMLineIndex,
        candidate: candidateData.candidate
      });

      try {
        await peerConnection.value.addIceCandidate(iceCandidate);
        console.log('ICE候选添加成功');
      } catch (addError) {
        console.error('添加ICE候选时出错:', addError);
        // 忽略一些常见的非致命错误
        if (addError.message.includes('already')) {
          console.log('ICE候选已经添加过，忽略');
        }
      }
    } else {
      console.warn('无效的ICE候选数据:', candidateData);
    }

  } catch (error) {
    console.error('处理ICE候选失败:', error);
    // 不显示错误消息，因为ICE候选错误通常不影响主要功能
  }
};

// 发起视频通话
const initiateCall = async (receiverId, receiverType) => {
  if (isInCall.value || isConnecting.value || isRinging.value) {
    ElMessage.warning('当前无法发起新的通话');
    return;
  }

  if (!receiverId || !receiverType) {
    ElMessage.warning('请填写用户ID和用户类型');
    return;
  }

  try {
    // 生成callId
    currentCallId.value = generateCallId();

    // 获取本地媒体流
    await getLocalStream();

    // 创建PeerConnection
    createPeerConnection(currentCallId.value, receiverId, receiverType);

    // 创建offer
    const offer = await peerConnection.value.createOffer({
      offerToReceiveVideo: true,
      offerToReceiveAudio: true
    });

    await peerConnection.value.setLocalDescription(offer);

    // 发送ringing状态
    sendStatus(receiverId, receiverType, 'ringing');

    // 发送offer
    sendSignal(receiverId, receiverType, 'offer', JSON.stringify(offer));

    isInCall.value = true;
    ElMessage.info('通话请求已发送，等待对方接受...');
  } catch (error) {
    console.error('发起通话失败:', error);
    ElMessage.error('发起通话失败: ' + error.message);
    handleCallEnded();
  }
};

// 结束通话
const endCall = () => {
  if (!isInCall.value && !isRinging.value) return;

  // 发送ended状态
  if (callerId.value && currentCallId.value) {
    sendStatus(callerId.value, callerType.value, 'ended');
  }

  handleCallEnded();
};

// 处理通话结束
const handleCallEnded = () => {
  isInCall.value = false;
  isRinging.value = false;

  // 关闭PeerConnection
  if (peerConnection.value) {
    peerConnection.value.close();
    peerConnection.value = null;
  }

  // 停止媒体流
  stopLocalStream();

  // 重置状态
  currentCallId.value = null;
  callerId.value = null;
  callerType.value = null;

  ElMessage.info('通话已结束');
};

// 切换摄像头
const toggleCamera = () => {
  if (!localStream.value) return;

  try {
    const videoTracks = localStream.value.getVideoTracks();
    if (videoTracks.length > 0) {
      const track = videoTracks[0];
      track.enabled = !track.enabled;
      ElMessage.info(track.enabled ? '摄像头已开启' : '摄像头已关闭');
    }
  } catch (error) {
    console.error('切换摄像头失败:', error);
    ElMessage.error('切换摄像头失败');
  }
};

// 切换麦克风
const toggleMicrophone = () => {
  if (!localStream.value) return;

  try {
    const audioTracks = localStream.value.getAudioTracks();
    if (audioTracks.length > 0) {
      const track = audioTracks[0];
      track.enabled = !track.enabled;
      ElMessage.info(track.enabled ? '麦克风已开启' : '麦克风已关闭');
    }
  } catch (error) {
    console.error('切换麦克风失败:', error);
    ElMessage.error('切换麦克风失败');
  }
};

// 组件挂载时
onMounted(async () => {
  // 获取用户信息
  getCounselorInfo();
  console.log('视频通话组件挂载，用户信息:', { userId: userId.value, userType: userType.value });

  // 连接WebSocket
  connectWebSocket();
});

// 组件卸载时
onUnmounted(() => {
  // 结束通话
  endCall();

  // 断开WebSocket连接
  disconnectWebSocket();
});
</script>

<template>
  <div class="video-call-container">
    <h2>视频通话系统</h2>
    
    <!-- 连接状态 -->
    <div class="connection-status">
      <el-badge :value="isConnected ? '已连接' : '未连接'" :type="isConnected ? 'success' : 'danger'" class="status-badge">
        WebSocket状态
      </el-badge>
      <el-button 
        :disabled="isConnected || isConnecting"
        @click="connectWebSocket"
        type="primary"
        :loading="isConnecting"
      >
        连接服务器
      </el-button>
      <el-button 
        :disabled="!isConnected"
        @click="disconnectWebSocket"
        type="danger"
      >
        断开连接
      </el-button>
    </div>
    
    <!-- 通话设置面板 -->
    <div class="call-panel" v-if="!isInCall && !isRinging">
      <el-form label-width="80px">
        <el-form-item label="用户ID">
          <el-input v-model="callerId" placeholder="请输入要呼叫的用户ID"></el-input>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="callerType" placeholder="请选择用户类型">
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="咨询师" value="COUNSELOR"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="success" 
            @click="initiateCall(callerId, callerType)"
            :disabled="!isConnected || !callerId || !callerType"
          >
            发起视频通话
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 视频通话界面 -->
    <div class="video-container" v-if="isInCall">
      <!-- 远程视频 -->
      <div class="remote-video-wrapper">
        <video 
          ref="remoteVideoRef"
          autoplay=""
          playsinline 
          class="remote-video"
        ></video>
        <div class="video-overlay">
          <span class="connection-status-text">
            {{ peerConnection?.connectionState === 'connected' ? '已连接' : '连接中...' }}
          </span>
        </div>
      </div>
      
      <!-- 本地视频小窗口 -->
      <div class="local-video-wrapper">
        <video 
          ref="localVideoRef"
          autoplay=""
          playsinline
          muted=""
          class="local-video"
        ></video>
      </div>
      
      <!-- 控制按钮 -->
      <div class="call-controls">
        <el-button 
          @click="toggleMicrophone" 
          type="primary" 
          circle 
          icon="el-icon-microphone"
        ></el-button>
        <el-button 
          @click="toggleCamera" 
          type="primary" 
          circle 
          icon="el-icon-video-camera"
        ></el-button>
        <el-button 
          @click="endCall" 
          type="danger" 
          circle 
          icon="el-icon-close"
        ></el-button>
      </div>
    </div>
    
    <!-- 来电提示 -->
    <div class="ringing-overlay" v-if="isRinging">
      <div class="ringing-card">
        <div class="ringing-icon">📞</div>
        <h3>视频通话来电</h3>
        <p>用户ID: {{ callerId }}</p>
        <div class="ringing-controls">
          <el-button 
            type="success" 
            icon="el-icon-check" 
            @click="acceptCall"
          >
            接受
          </el-button>
          <el-button 
            type="danger" 
            icon="el-icon-close" 
            @click="rejectCall"
          >
            拒绝
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 调试信息 -->
    <div class="debug-info">
      <h4>调试信息</h4>
      <p>用户ID: {{ userId }}</p>
      <p>用户类型: {{ userType }}</p>
      <p>连接状态: {{ isConnected ? '已连接' : '未连接' }}</p>
      <p>通话状态: {{ isInCall ? '通话中' : '空闲' }}</p>
      <p v-if="currentCallId">通话ID: {{ currentCallId }}</p>
      <p v-if="peerConnection">P2P状态: {{ peerConnection.connectionState }}</p>
    </div>
  </div>
</template>

<style scoped>
.video-call-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.video-call-container h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.status-badge {
  font-size: 16px;
  font-weight: 500;
}

.call-panel {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  margin: 0 auto;
}

.video-container {
  position: relative;
  width: 100%;
  height: 70vh;
  background-color: #000;
  border-radius: 8px;
  overflow: hidden;
}

.remote-video-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.remote-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.local-video-wrapper {
  position: absolute;
  bottom: 20px;
  right: 20px;
  width: 200px;
  height: 150px;
  background-color: #333;
  border: 2px solid #fff;
  border-radius: 8px;
  overflow: hidden;
  z-index: 10;
}

.local-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.call-controls {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 20px;
  z-index: 20;
}

.call-controls .el-button {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.ringing-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.ringing-card {
  background-color: #fff;
  padding: 40px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  max-width: 400px;
  width: 90%;
}

.ringing-icon {
  font-size: 64px;
  margin-bottom: 20px;
  animation: ring 1.5s infinite;
}

@keyframes ring {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.ringing-card h3 {
  margin-bottom: 10px;
  color: #303133;
}

.ringing-card p {
  color: #606266;
  margin-bottom: 30px;
}

.ringing-controls {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.ringing-controls .el-button {
  min-width: 100px;
}

.debug-info {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  font-family: monospace;
}

.debug-info h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.debug-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

@media (max-width: 768px) {
  .video-container {
    height: 60vh;
  }
  
  .local-video-wrapper {
    width: 120px;
    height: 90px;
    bottom: 10px;
    right: 10px;
  }
  
  .call-controls {
    bottom: 10px;
  }
  
  .call-controls .el-button {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
  
  .connection-status {
    flex-wrap: wrap;
  }
}
</style>