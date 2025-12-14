<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { User, Search} from '@element-plus/icons-vue';
import axios from 'axios';
import {ElMessage} from "element-plus";

// 路由实例
const router = useRouter();
const route = useRoute();

// 状态变量
const counselors = ref([]);
const loading = ref(false);
const searchText = ref('');
const userId = ref(null);
const selectedCounselor = ref(null);

// 计算过滤后的咨询师列表
const filteredCounselors = computed(() => {
  if (!searchText.value) return counselors.value;
  return counselors.value.filter(counselor => 
    counselor.nickname.toLowerCase().includes(searchText.value.toLowerCase()) ||
    counselor.lastMessage?.toLowerCase().includes(searchText.value.toLowerCase())
  );
});

// 从localStorage获取用户信息
const getUserInfo = () => {
  try {
    const storedCounselor = localStorage.getItem('counselor');
    if (storedCounselor) {
      const userInfo = JSON.parse(storedCounselor);
      userId.value = userInfo.counselorId || userInfo.id || 1;
    } else {
      userId.value = 1;
      console.warn('未找到用户信息，使用默认ID');
    }
  } catch (err) {
    console.error('解析用户信息失败:', err);
    userId.value = 1;
    ElMessage.warning('用户信息获取失败，使用默认设置');
  }
};

// 获取咨询师列表
const fetchCounselors = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/consultation/messages/user/counselors', {
      params: {
        userId: userId.value,
        type: 'counselor'
      },
      timeout: 10000
    });

    if (response.data.code === 200) {
      counselors.value = response.data.data || [];
    } else {
      throw new Error(response.data.message || '获取消息列表失败');
    }
  } catch (error) {
    ElMessage.error('获取消息列表失败：' + error.message);
    console.error('获取消息列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 格式化最后消息时间
const formatLastMessageTime = (timeString) => {
  if (!timeString) return '';
  const time = new Date(timeString);
  const now = new Date();
  const diff = now - time;

  // 小于1小时显示分钟
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000);
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`;
  }

  // 今天内显示时间
  if (time.toDateString() === now.toDateString()) {
    return time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  }

  // 昨天显示昨天
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (time.toDateString() === yesterday.toDateString()) {
    return '昨天';
  }

  // 一周内显示星期几
  if (diff < 7 * 24 * 3600000) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return weekdays[time.getDay()];
  }

  // 否则显示日期
  return time.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
};

// 搜索处理
const handleSearch = () => {
  console.log('搜索:', searchText.value);
};

// 开始聊天
const startChat = (counselor) => {
  if (!counselor || !counselor.id) {
    ElMessage.warning('用户信息不完整');
    return;
  }
  
  selectedCounselor.value = counselor;

  router.push({
    path: '/view/messages/chat-detail',
    query: {
      userId: counselor.id,
      counselorId: userId.value,
      nickname: encodeURIComponent(counselor.nickname || '用户'),
      avatar: encodeURIComponent(counselor.avatarUrl || '')
    }
  });
};

// 组件挂载时初始化数据
onMounted(() => {
  getUserInfo();
  fetchCounselors();
});
</script>

<template>
  <div class="chat-container">
    <!-- 左侧消息列表 -->
    <div class="message-list-container">
      <!-- 头部 -->
      <div class="header">
        <h1>消息</h1>
      </div>

      <!-- 搜索栏 -->
      <div class="search-section">
        <el-input
            v-model="searchText"
            placeholder="搜索"
            clearable
            size="large"
            @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <!-- 咨询师列表 -->
      <div v-if="loading" class="loading-section">
        <el-skeleton class="skeleton-item" v-for="i in 6" :key="i" :rows="3" animated />
      </div>

      <div v-else-if="counselors.length === 0" class="empty-section">
        <el-empty description="暂无聊天记录">
          <template #image>
            <el-icon :size="80" color="#C0C4CC">
              <User />
            </el-icon>
          </template>
          <p class="empty-tip">您还没有与任何咨询师进行过对话</p>
          <el-button type="primary" @click="fetchCounselors">刷新查看</el-button>
        </el-empty>
      </div>

      <div v-else class="counselors-list">
        <div
            v-for="counselor in filteredCounselors"
            :key="counselor.id"
            class="counselor-item"
            :class="{'selected': selectedCounselor?.id === counselor.id}"
            @click="startChat(counselor)"
        >
          <!-- 头像和状态 -->
          <div class="avatar-wrapper">
            <el-avatar
                :size="60"
                :src="counselor.avatarUrl"
                class="avatar"
            >
              {{ counselor.nickname.charAt(0).toUpperCase() }}
            </el-avatar>
            <div
                class="status-indicator"
                :class="{
                'online': counselor.status === 'ACTIVE',
                'offline': counselor.status !== 'ACTIVE'
              }"
            ></div>
          </div>

          <!-- 咨询师信息 -->
          <div class="info-section">
            <div class="top-row">
              <h3 class="name">{{ counselor.nickname }}</h3>
              <span v-if="counselor.lastMessageTime" class="time-text">{{ formatLastMessageTime(counselor.lastMessageTime) }}</span>
            </div>
            <div class="bottom-row">
              <span v-if="counselor.lastMessage" class="last-message">{{ counselor.lastMessage }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天详情 -->
    <div class="chat-detail-container">
      <router-view v-if="route.matched.length > 2" />
      <div v-else class="chat-detail-placeholder">
        <el-empty description="请选择一个聊天">
          <template #image>
            <el-icon :size="120" color="#C0C4CC">
              <User />
            </el-icon>
          </template>
          <p class="empty-tip">从左侧选择一个联系人开始聊天</p>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background-color: #f0f2f5;
}

.message-list-container {
  background-color: #f0f2f5;
  width: 320px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e4e7ed;
  overflow: hidden;
}

.header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;

  h1 {
    margin: 0;
    color: #303133;
    font-size: 20px;
    font-weight: 600;
  }
}

.search-section {
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.loading-section {
  padding: 16px;
  background-color: #fff;

  .skeleton-item {
    margin-bottom: 12px;
    border-radius: 8px;
  }
}

.empty-section {
  padding: 40px 0;
  text-align: center;
  background-color: #fff;

  .empty-tip {
    margin-top: 12px;
    color: #909399;
    font-size: 14px;
  }
}

.counselors-list {
  flex: 1;
  overflow-y: auto;
  background-color: #fff;
}

.counselor-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-bottom: 1px solid #f0f0f0;

  &:hover {
    background-color: #f5f7fa;
  }
  
  &.selected {
    background-color: #e6f7ff;
  }

  .avatar-wrapper {
    position: relative;
    margin-right: 12px;
    flex-shrink: 0;

    .avatar {
      border: 2px solid #f0f0f0;
    }

    .status-indicator {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      border: 2px solid white;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      &.online {
        background-color: #67C23A;
      }

      &.offline {
        background-color: #909399;
      }
    }
  }

  .info-section {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .top-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .name {
        margin: 0;
        color: #303133;
        font-size: 16px;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .time-text {
        color: #C0C4CC;
        font-size: 12px;
      }
    }

    .bottom-row {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .last-message {
        flex: 1;
        color: #606266;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

/* 滚动条样式 */
.counselors-list::-webkit-scrollbar {
  width: 4px;
}

.counselors-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.counselors-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.counselors-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.counselor-item {
  animation: fadeIn 0.3s ease-out forwards;
  opacity: 0;

  @for $i from 1 through 10 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.1}s;
    }
  }
}

/* 右侧聊天详情容器 */
.chat-detail-container {
  flex: 1;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  position: relative;
}

.chat-detail-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-color: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }
  
  .message-list-container {
    width: 100%;
    height: 100vh;
  }

  .header {
    padding: 12px 16px;

    h1 {
      font-size: 18px;
    }
  }

  .search-bar {
    padding: 8px 16px;

    .el-input {
      width: 100%;
    }
  }

  .counselors-list {
    padding: 0;

    .counselor-item {
      padding: 12px 16px;

      .avatar-section {
        margin-right: 12px;

        .el-avatar {
          size: 40px;
        }
      }

      .info-section {
        flex: 1;

        .name {
          font-size: 14px;
        }

        .last-message {
          font-size: 12px;
          line-height: 1.3;
        }
      }

      .meta-section {
        .time {
          font-size: 11px;
        }
      }
    }
  }
}
</style>