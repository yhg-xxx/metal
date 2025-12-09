<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElCard, ElButton, ElSkeleton, ElEmpty } from 'element-plus'
import { Message, User, Phone, Message as ChatIcon } from '@element-plus/icons-vue'
import axios from 'axios';

// 路由实例
const router = useRouter()

// 响应式数据
const counselors = ref([])
const loading = ref(false)
const userId = ref(null)
const error = ref(null)

// 从localStorage获取用户信息
const getUserInfo = () => {
  try {
    const storedCounselor = localStorage.getItem('counselor')
    if (storedCounselor) {
      const userInfo = JSON.parse(storedCounselor)
      userId.value = userInfo.counselorId || userInfo.id || 1
    } else {
      userId.value = 1
      console.warn('未找到用户信息，使用默认ID')
    }
  } catch (err) {
    console.error('解析用户信息失败:', err)
    userId.value = 1
    ElMessage.warning('用户信息获取失败，使用默认设置')
  }
}

// 获取咨询师列表
const fetchCounselors = async () => {
  loading.value = true
  error.value = null

  try {
    const response = await axios.get('/api/consultation/messages/user/counselors', {
      params: {
        userId: userId.value,
        type: 'counselor'
      },
      timeout: 10000 // 10秒超时
    })

    if (response.data.code === 200) {
      counselors.value = response.data.data || []
      if (counselors.value.length === 0) {
        ElMessage.info('暂无咨询师消息')
      }
    } else {
      throw new Error(response.data.message || '获取咨询师列表失败')
    }
  } catch (err) {
    error.value = err.message || '请求失败'
    ElMessage.error(`获取咨询师列表失败：${err.message}`)
    console.error('获取咨询师列表失败:', err)
  } finally {
    loading.value = false
  }
}

// 跳转到聊天详情页
const goToChatDetail = (user) => {
  if (!user || !user.id) {
    ElMessage.warning('用户信息不完整')
    return
  }

  router.push({
    path: '/view/chat-detail',
    query: {
      userId: user.id,
      counselorId: userId.value,
      nickname: encodeURIComponent(user.nickname || '用户'),
      avatar: encodeURIComponent(user.avatarUrl || '')
    }
  })
}

// 刷新数据
const refreshData = () => {
  fetchCounselors()
}

// 计算属性：过滤在线咨询师
const onlineCounselors = computed(() => {
  return counselors.value.filter(c => c.status === 'ACTIVE')
})

// 计算属性：按最后消息时间排序
const sortedCounselors = computed(() => {
  return [...counselors.value].sort((a, b) => {
    // 如果都有最后消息时间，按时间倒序
    if (a.lastMessageTime && b.lastMessageTime) {
      return new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
    }
    // 在线优先
    if (a.status === 'ACTIVE' && b.status !== 'ACTIVE') return -1
    if (a.status !== 'ACTIVE' && b.status === 'ACTIVE') return 1
    return 0
  })
})

// 格式化显示时间
const formatTime = (timeString) => {
  if (!timeString) return ''

  const time = new Date(timeString)
  const now = new Date()
  const diff = now - time

  // 小于1小时显示分钟
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`
  }

  // 今天内显示时间
  if (time.toDateString() === now.toDateString()) {
    return time.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }

  // 昨天显示昨天
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  if (time.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  // 一周内显示星期几
  if (diff < 7 * 24 * 3600000) {
    const weekdays = ['日', '一', '二', '三', '四', '五', '六']
    return `星期${weekdays[time.getDay()]}`
  }

  // 否则显示日期
  return time.toLocaleDateString('zh-CN')
}

// 处理头像加载失败
const handleAvatarError = (event, defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png') => {
  event.target.src = defaultAvatar
}

// 组件挂载时初始化数据
onMounted(() => {
  getUserInfo()
  fetchCounselors()
})
</script>

<template>
  <div class="message-list-container">
    <!-- 标题栏 -->
    <div class="header">
      <div class="title-section">
        <el-icon class="title-icon" :size="24">
          <Message />
        </el-icon>
        <h2>消息列表</h2>
        <el-tag v-if="onlineCounselors.length > 0" type="success" size="small">
          {{ onlineCounselors.length }} 人在线
        </el-tag>
      </div>

      <el-button
          type="primary"
          :icon="ChatIcon"
          @click="refreshData"
          :loading="loading"
      >
        刷新列表
      </el-button>
    </div>

    <!-- 错误提示 -->
    <el-alert
        v-if="error"
        :title="error"
        type="error"
        :closable="true"
        show-icon
        class="error-alert"
    />

    <!-- 咨询师列表 -->
    <div class="content-wrapper">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <el-skeleton class="skeleton-item" v-for="i in 6" :key="i" :rows="3" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="counselors.length === 0" class="empty-section">
        <el-empty description="暂无咨询师消息">
          <template #image>
            <el-icon :size="80" color="#C0C4CC">
              <User />
            </el-icon>
          </template>
          <p class="empty-tip">您还没有与任何咨询师进行过对话</p>
          <el-button type="primary" @click="refreshData">刷新查看</el-button>
        </el-empty>
      </div>

      <!-- 咨询师卡片列表 -->
      <div v-else class="counselors-grid">
        <el-card
            v-for="counselor in sortedCounselors"
            :key="counselor.id"
            class="counselor-card"
            :class="{ 'online': counselor.status === 'ACTIVE' }"
            shadow="hover"
            @click="goToChatDetail(counselor)"
        >
          <!-- 在线状态指示器 -->
          <div class="status-indicator" :class="counselor.status === 'ACTIVE' ? 'online' : 'offline'"></div>

          <div class="counselor-content">
            <!-- 头像部分 -->
            <div class="avatar-section">
              <div class="avatar-wrapper">
                <img
                    :src="counselor.avatarUrl"
                    :alt="counselor.nickname"
                    class="avatar"
                    @error="(e) => handleAvatarError(e)"
                />
                <div class="status-badge" :class="counselor.status === 'ACTIVE' ? 'online' : 'offline'"></div>
              </div>
            </div>

            <!-- 信息部分 -->
            <div class="info-section">
              <div class="name-row">
                <h3 class="nickname">{{ counselor.nickname }}</h3>
                <el-tag
                    v-if="counselor.expertise"
                    size="small"
                    type="info"
                    effect="plain"
                    class="expertise-tag"
                >
                  {{ counselor.expertise }}
                </el-tag>
              </div>

              <div class="contact-row">
                <el-icon class="contact-icon" :size="14">
                  <Phone />
                </el-icon>
                <span class="contact-text">{{ counselor.phone || '暂无电话' }}</span>
              </div>

              <div class="meta-row">
                <span class="gender-badge" :class="counselor.gender === 'MALE' ? 'male' : 'female'">
                  {{ counselor.gender === 'MALE' ? '男' : '女' }}
                </span>
                <span class="age">{{ counselor.age || '未知' }}岁</span>
                <span class="last-message" v-if="counselor.lastMessage">
                  {{ counselor.lastMessage }}
                </span>
              </div>

              <div class="time-row" v-if="counselor.lastMessageTime">
                <span class="time-text">{{ formatTime(counselor.lastMessageTime) }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-section">
              <el-button
                  type="primary"
                  :icon="ChatIcon"
                  class="chat-btn"
                  @click.stop="goToChatDetail(counselor)"
                  :disabled="loading"
              >
                开始聊天
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 底部统计 -->
    <div v-if="!loading && counselors.length > 0" class="footer-stats">
      <span>共 {{ counselors.length }} 位咨询师</span>
      <span class="online-count">在线 {{ onlineCounselors.length }} 人</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.message-list-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: 100vh;
  padding: 24px;
  border-radius: 12px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;

  .title-section {
    display: flex;
    align-items: center;
    gap: 12px;

    .title-icon {
      color: #409EFF;
    }

    h2 {
      margin: 0;
      color: #303133;
      font-size: 24px;
      font-weight: 600;
      background: linear-gradient(45deg, #409EFF, #67C23A);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
}

.error-alert {
  margin-bottom: 20px;
  border-radius: 8px;
}

.content-wrapper {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  min-height: 400px;
}

.loading-section {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;

  .skeleton-item {
    padding: 20px;
    border-radius: 8px;
  }
}

.empty-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;

  .empty-tip {
    margin-top: 12px;
    color: #909399;
    font-size: 14px;
  }
}

.counselors-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.counselor-card {
  position: relative;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;

  &.online {
    border-left: 4px solid #67C23A;
  }

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);

    .counselor-content {
      background: linear-gradient(to right, #fafafa, #f0f9ff);
    }
  }

  .status-indicator {
    position: absolute;
    top: 0;
    right: 0;
    width: 8px;
    height: 100%;

    &.online {
      background: linear-gradient(to bottom, #67C23A, #85ce61);
    }

    &.offline {
      background: linear-gradient(to bottom, #909399, #c0c4cc);
    }
  }

  .counselor-content {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    transition: background-color 0.3s;

    .avatar-section {
      flex-shrink: 0;

      .avatar-wrapper {
        position: relative;

        .avatar {
          width: 72px;
          height: 72px;
          border-radius: 50%;
          object-fit: cover;
          border: 3px solid #e4e7ed;
          transition: border-color 0.3s;
        }

        .status-badge {
          position: absolute;
          bottom: 4px;
          right: 4px;
          width: 14px;
          height: 14px;
          border-radius: 50%;
          border: 2px solid white;

          &.online {
            background-color: #67C23A;
            box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.2);
          }

          &.offline {
            background-color: #909399;
            box-shadow: 0 0 0 2px rgba(144, 147, 153, 0.2);
          }
        }
      }
    }

    .info-section {
      flex: 1;
      min-width: 0;

      .name-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .nickname {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .expertise-tag {
          font-size: 12px;
          height: 20px;
          line-height: 18px;
        }
      }

      .contact-row {
        display: flex;
        align-items: center;
        gap: 6px;
        margin-bottom: 8px;
        color: #606266;
        font-size: 14px;

        .contact-icon {
          color: #909399;
        }

        .contact-text {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .meta-row {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;
        font-size: 13px;

        .gender-badge {
          padding: 2px 8px;
          border-radius: 10px;
          font-size: 12px;
          font-weight: 500;

          &.male {
            background-color: #ecf5ff;
            color: #409EFF;
          }

          &.female {
            background-color: #fef0f0;
            color: #F56C6C;
          }
        }

        .age {
          color: #909399;
        }

        .last-message {
          flex: 1;
          color: #606266;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          font-style: italic;
        }
      }

      .time-row {
        .time-text {
          color: #C0C4CC;
          font-size: 12px;
        }
      }
    }

    .action-section {
      flex-shrink: 0;

      .chat-btn {
        padding: 8px 16px;
        border-radius: 20px;
        font-weight: 500;
        transition: all 0.3s;

        &:hover {
          transform: scale(1.05);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }

        &:active {
          transform: scale(0.95);
        }
      }
    }
  }
}

.footer-stats {
  margin-top: 24px;
  padding: 16px 24px;
  background: white;
  border-radius: 8px;
  text-align: center;
  color: #606266;
  font-size: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .online-count {
    margin-left: 16px;
    color: #67C23A;
    font-weight: 500;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-list-container {
    padding: 16px;
  }

  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;

    .title-section {
      justify-content: center;
    }
  }

  .content-wrapper {
    padding: 16px;
  }

  .counselors-grid {
    grid-template-columns: 1fr;
  }

  .counselor-card {
    .counselor-content {
      flex-direction: column;
      text-align: center;
      gap: 12px;

      .avatar-section {
        .avatar-wrapper {
          .avatar {
            width: 80px;
            height: 80px;
          }
        }
      }

      .info-section {
        width: 100%;

        .meta-row {
          justify-content: center;
          flex-wrap: wrap;
        }
      }
    }
  }
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

.counselor-card {
  animation: fadeIn 0.5s ease-out forwards;
  opacity: 0;

  @for $i from 1 through 10 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.1}s;
    }
  }
}
</style>