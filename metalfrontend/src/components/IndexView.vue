<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import router from '@/router.js';
import axios from 'axios';

// 当前激活的菜单项
const activeMenu = ref('profile');

// 用户信息
const counselor = ref(null);

// 菜单列表
const menuItems = [
  {
    index: 'messages',
    title: '消息列表',
    icon: 'el-icon-chat-dot-round'
  },
  {
    index: 'profile',
    title: '个人信息',
    icon: 'el-icon-user'
  }
];

// 处理菜单点击
const handleMenuClick = (index) => {
  activeMenu.value = index;
  // 跳转到对应的路由
  router.push(`/view/${index}`);
};

// 退出登录
const handleLogout = () => {
  // 清除本地存储的token和用户信息
  localStorage.removeItem('token');
  localStorage.removeItem('counselor');
  // 清除axios的Authorization header
  delete axios.defaults.headers.common['Authorization'];
  // 跳转到登录页
  router.push('/');
  ElMessage.success('退出登录成功');
};

// 组件挂载时检查登录状态
onMounted(() => {
  // 从本地存储获取用户信息
  const storedCounselor = localStorage.getItem('counselor');
  const token = localStorage.getItem('token');
  
  if (!token || !storedCounselor) {
    // 如果没有登录信息，跳转到登录页
    ElMessage.error('请先登录');
    router.push('/');
    return;
  }
  
  // 设置用户信息
  counselor.value = JSON.parse(storedCounselor);
  
  // 检查当前路由，设置激活的菜单
  const currentPath = router.currentRoute.value.path.split('/').pop();
  if (currentPath) {
    activeMenu.value = currentPath;
  }
  
  // 设置axios的Authorization header
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
});

// 解析擅长领域和治疗流派
const parseJsonString = (jsonString) => {
  try {
    return JSON.parse(jsonString);
  } catch (e) {
    return [];
  }
};
</script>

<template>
  <div class="index-container">
    <!-- 侧边栏导航 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3 class="logo">心理咨询系统</h3>
      </div>
      
      <div class="sidebar-menu">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          @select="handleMenuClick"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.index"
            :index="item.index"
          >
            <i :class="item.icon"></i>
            <span slot="title">{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="top-navbar">
        <div class="user-info">
          <div class="user-avatar" v-if="counselor">
            <img :src="counselor.avatarUrl" alt="头像" v-if="counselor.avatarUrl" />
            <span v-else>{{ counselor.nickname?.[0] || 'C' }}</span>
          </div>
          <div class="user-details">
            <p class="nickname">{{ counselor?.nickname || '未登录' }}</p>
            <p class="role">心理咨询师</p>
          </div>
          <el-button type="text" @click="handleLogout" class="logout-btn">退出登录</el-button>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="content-wrapper">
        <!-- 子路由视图 -->
        <RouterView />
      </div>
    </div>
  </div>
</template>

<style scoped>
.index-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background-color: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #1f2937;
}

.logo {
  margin: 0;
  color: #fff;
  font-size: 18px;
  text-align: center;
}

.sidebar-menu {
  flex: 1;
  padding-top: 20px;
}

.el-menu-vertical {
  background-color: #001529;
  border-right: none;
}

.el-menu-item {
  color: rgba(255, 255, 255, 0.65);
  height: 60px;
  line-height: 60px;
}

.el-menu-item:hover {
  background-color: #1890ff;
  color: #fff;
}

.el-menu-item.is-active {
  background-color: #1890ff;
  color: #fff;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
  overflow: hidden;
}

.top-navbar {
  height: 64px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #1890ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  margin-right: 12px;
  overflow: hidden;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  margin-right: 20px;
}

.nickname {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.role {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.logout-btn {
  color: #606266;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 欢迎区域样式 */
.welcome-section {
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.welcome-section h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.counselor-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-card,
.introduction-card {
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
}

.info-card h3,
.introduction-card h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
  align-items: flex-start;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  width: 100px;
  color: #606266;
  flex-shrink: 0;
}

.value {
  color: #303133;
  flex: 1;
}

.fee {
  color: #f56c6c;
  font-weight: 500;
}

.rating {
  color: #e6a23c;
  font-weight: 500;
}

.introduction {
  color: #303133;
  line-height: 1.6;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .index-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
  }
  
  .sidebar-menu {
    padding-top: 0;
  }
  
  .counselor-info {
    grid-template-columns: 1fr;
  }
}
</style>