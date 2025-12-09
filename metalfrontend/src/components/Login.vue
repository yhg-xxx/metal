<script setup>
import { ref, reactive } from 'vue';
import { ElMessage, ElLoading } from 'element-plus';
import router from '@/router.js';
import axios from 'axios';

// 表单数据
const form = reactive({
  username: '',
  password: ''
});

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^counselor_/, message: '用户名必须以counselor_开头', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

// 表单引用
const formRef = ref(null);

// 登录方法
const handleLogin = async () => {
  // 表单验证
  formRef.value.validate(async (valid) => {
    if (valid) {
      // 显示加载动画
      const loading = ElLoading.service({
        lock: true,
        text: '登录中...',
        background: 'rgba(0, 0, 0, 0.7)'
      });

      try {
        // 调用登录接口
        const response = await axios.post('/api/auth/counselor/login', form);
        const { success, token, message, counselor } = response.data;

        if (success) {
          // 存储token和用户信息
          localStorage.setItem('token', token);
          localStorage.setItem('counselor', JSON.stringify(counselor));
          // 设置axios默认headers
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
          
          ElMessage.success(message);
          // 跳转到首页
          await router.push('/view');
        } else {
          ElMessage.error(message);
        }
      } catch (error) {
        console.error('登录失败:', error);
        ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试');
      } finally {
        // 关闭加载动画
        loading.close();
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  formRef.value.resetFields();
};
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">心理咨询师登录</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名 (counselor_开头)"
            prefix-icon="el-icon-user"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            @click="handleLogin"
          >
            登录
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-box {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
}

.el-form-item {
  margin-bottom: 25px;
}
</style>