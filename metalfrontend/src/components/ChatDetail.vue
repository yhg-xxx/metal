<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElSkeleton } from 'element-plus';
import axios from 'axios';

// 路由实例
const route = useRoute();
// 聊天记录数据
const messages = ref([]);
// 加载状态
const loading = ref(false);
// 用户类型，当前登录用户是咨询师
const currentUserType = ref('COUNSELOR');

// 从路由参数获取userId和counselorId
const userId = computed(() => route.query.userId || 26);
const counselorId = computed(() => route.query.counselorId || 11);

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
      messages.value = response.data;
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

// 格式化时间
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

// 判断消息是否来自当前用户
const isCurrentUser = (senderType) => {
  return senderType === currentUserType.value;
};

// 组件挂载时获取聊天记录
onMounted(() => {
  fetchMessages();
});
</script>

<template>
  <div class="chat-detail-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <h2>聊天详情</h2>
      <div class="chat-info">
        <span class="user-id">用户ID：{{ userId }}</span>
        <span class="counselor-id">咨询师ID：{{ counselorId }}</span>
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
          :class="{ 'current-user': isCurrentUser(message.senderType) }"
        >
          <div class="message-content">
            <div class="message-bubble">
              <p class="content">{{ message.content }}</p>
              <span class="time">{{ formatTime(message.sentTime) }}</span>
            </div>
            <div class="sender-type">
              {{ message.senderType === 'COUNSELOR' ? '咨询师' : '用户' }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else class="empty-messages">
        <el-empty description="暂无聊天记录" />
      </div>
    </div>
    
    <!-- 消息输入区域（暂时隐藏） -->
    <div class="chat-input-area" style="display: none;">
      <el-input
        v-model="newMessage"
        type="textarea"
        placeholder="输入消息..."
        :rows="3"
        maxlength="500"
        show-word-limit
      />
      <div class="input-actions">
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-detail-container {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 聊天头部样式 */
.chat-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.chat-header h2 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 20px;
  font-weight: 500;
}

.chat-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
}

/* 聊天记录区域样式 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  padding-right: 8px;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 加载骨架屏 */
.loading-skeleton {
  padding: 10px 0;
}

/* 空状态样式 */
.empty-messages {
  padding: 40px 0;
  text-align: center;
}

/* 消息列表样式 */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息项样式 */
.message-item {
  display: flex;
  margin-bottom: 16px;
}

/* 当前用户消息样式 */
.message-item.current-user {
  justify-content: flex-end;
}

/* 消息内容样式 */
.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-item.current-user .message-content {
  align-items: flex-end;
}

/* 消息气泡样式 */
.message-bubble {
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
  background-color: #f0f0f0;
  color: #303133;
}

.message-item.current-user .message-bubble {
  background-color: #1890ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-item:not(.current-user) .message-bubble {
  background-color: #f0f0f0;
  border-bottom-left-radius: 4px;
}

/* 消息内容文本 */
.content {
  margin: 0 0 8px 0;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;
}

/* 消息时间 */
.time {
  font-size: 12px;
  opacity: 0.7;
  display: block;
  text-align: right;
}

/* 发送者类型 */
.sender-type {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.message-item.current-user .sender-type {
  text-align: right;
}

/* 消息输入区域样式（暂时隐藏） */
.chat-input-area {
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-detail-container {
    padding: 16px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-info {
    flex-direction: column;
    gap: 8px;
  }
}
</style>