<script setup>
import { ref, onMounted } from 'vue';

// 咨询师信息
const counselor = ref(null);

// 解析JSON字符串
const parseJsonString = (jsonString) => {
  try {
    return JSON.parse(jsonString);
  } catch (e) {
    return [];
  }
};

// 初始化数据
onMounted(() => {
  // 从本地存储获取咨询师信息
  const storedCounselor = localStorage.getItem('counselor');
  if (storedCounselor) {
    counselor.value = JSON.parse(storedCounselor);
  }
});
</script>

<template>
  <div class="profile-container">
    <h2>个人信息</h2>
    
    <div v-if="counselor" class="profile-info">
      <div class="avatar-section">
        <img :src="counselor.avatarUrl" :alt="counselor.nickname" class="avatar" />
        <h3>{{ counselor.nickname }}</h3>
        <p class="user-id">用户ID：{{ counselor.userId }}</p>
      </div>
      
      <div class="info-section">
        <div class="info-card">
          <h4>基本信息</h4>
          <div class="info-item">
            <span class="label">用户名：</span>
            <span class="value">{{ counselor.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">真实姓名：</span>
            <span class="value">{{ counselor.realName }}</span>
          </div>
          <div class="info-item">
            <span class="label">咨询师ID：</span>
            <span class="value">{{ counselor.counselorId }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号：</span>
            <span class="value">{{ counselor.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱：</span>
            <span class="value">{{ counselor.email }}</span>
          </div>
          <div class="info-item">
            <span class="label">性别：</span>
            <span class="value">{{ counselor.gender === 'MALE' ? '男' : '女' }}</span>
          </div>
          <div class="info-item">
            <span class="label">年龄：</span>
            <span class="value">{{ counselor.age }}岁</span>
          </div>
          <div class="info-item">
            <span class="label">状态：</span>
            <span class="value">{{ counselor.userStatus === 'ACTIVE' ? '在线' : '离线' }}</span>
          </div>
          <div class="info-item">
            <span class="label">身份证号：</span>
            <span class="value">{{ counselor.idNumber }}</span>
          </div>
        </div>
        
        <div class="info-card">
          <h4>资质信息</h4>
          <div class="info-item">
            <span class="label">从业年限：</span>
            <span class="value">{{ counselor.yearsOfExperience }}年</span>
          </div>
          <div class="info-item">
            <span class="label">资格证书：</span>
            <span class="value">
              <a :href="counselor.qualificationCertificateUrl" target="_blank">查看证书</a>
            </span>
          </div>
          <div class="info-item">
            <span class="label">执业证书：</span>
            <span class="value">
              <a :href="counselor.practiceCertificateUrl" target="_blank">查看证书</a>
            </span>
          </div>
          <div class="info-item">
            <span class="label">认证照片：</span>
            <span class="value">
              <a :href="counselor.photoUrl" target="_blank">查看照片</a>
            </span>
          </div>
          <div class="info-item">
            <span class="label">审批时间：</span>
            <span class="value">{{ new Date(counselor.approvedTime).toLocaleString() }}</span>
          </div>
        </div>
        
        <div class="info-card">
          <h4>专业信息</h4>
          <div class="info-item">
            <span class="label">擅长领域：</span>
            <span class="value" v-for="(field, index) in parseJsonString(counselor.specialization)" :key="index">
              {{ field }}<span v-if="index < parseJsonString(counselor.specialization).length - 1">、</span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">治疗流派：</span>
            <span class="value" v-for="(approach, index) in parseJsonString(counselor.therapeuticApproach)" :key="index">
              {{ approach }}<span v-if="index < parseJsonString(counselor.therapeuticApproach).length - 1">、</span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">咨询费用：</span>
            <span class="value fee">¥{{ counselor.consultationFee }}/次</span>
          </div>
          <div class="info-item">
            <span class="label">平均评分：</span>
            <span class="value rating">{{ counselor.rating }} ★</span>
          </div>
          <div class="info-item">
            <span class="label">总咨询次数：</span>
            <span class="value">{{ counselor.totalSessions }}次</span>
          </div>
        </div>
        
        <div class="info-card">
          <h4>服务信息</h4>
          <div class="info-item">
            <span class="label">服务类型：</span>
            <span class="value" v-for="(type, index) in parseJsonString(counselor.serviceTypes)" :key="index">
              {{ type === 'TEXT' ? '文字' : type === 'VOICE' ? '语音' : '视频' }}<span v-if="index < parseJsonString(counselor.serviceTypes).length - 1">、</span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">可用天数：</span>
            <span class="value" v-for="(day, index) in parseJsonString(counselor.availableDays)" :key="index">
              {{ day === 'MONDAY' ? '周一' : day === 'TUESDAY' ? '周二' : day === 'WEDNESDAY' ? '周三' : day === 'THURSDAY' ? '周四' : day === 'FRIDAY' ? '周五' : day === 'SATURDAY' ? '周六' : '周日' }}<span v-if="index < parseJsonString(counselor.availableDays).length - 1">、</span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">工作时间：</span>
            <span class="value">
              {{ JSON.parse(counselor.workingHours).start }} - {{ JSON.parse(counselor.workingHours).end }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">咨询时长：</span>
            <span class="value" v-for="(duration, index) in parseJsonString(counselor.sessionDurations)" :key="index">
              {{ duration }}分钟<span v-if="index < parseJsonString(counselor.sessionDurations).length - 1">、</span>
            </span>
          </div>
          <div class="info-item">
            <span class="label">每日最大咨询数：</span>
            <span class="value">{{ counselor.maxDailySessions }}次</span>
          </div>
        </div>
        
        <div class="info-card">
          <h4>个人介绍</h4>
          <div class="introduction">
            {{ counselor.introduction }}
          </div>
        </div>
        
        <div class="info-card">
          <h4>系统信息</h4>
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ new Date(counselor.createdTime).toLocaleString() }}</span>
          </div>
          <div class="info-item">
            <span class="label">更新时间：</span>
            <span class="value">{{ new Date(counselor.updatedTime).toLocaleString() }}</span>
          </div>
          <div class="info-item">
            <span class="label">服务设置ID：</span>
            <span class="value">{{ counselor.serviceSettingsId }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-info">
      <el-empty description="暂无个人信息" />
    </div>
  </div>
</template>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-container h2 {
  margin-top: 0;
  margin-bottom: 24px;
  color: #303133;
  text-align: center;
}

.profile-info {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

/* 头像区域样式 */
.avatar-section {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 12px;
  border: 3px solid #f0f0f0;
}

.avatar-section h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 20px;
}

.user-id {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 信息区域样式 */
.info-section {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

/* 信息卡片样式 */
.info-card {
  background-color: #fafafa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.info-card h4 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
  border-bottom: 1px solid #e8e8e8;
  padding-bottom: 8px;
}

/* 信息项样式 */
.info-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.label {
  width: 120px;
  color: #606266;
  font-size: 14px;
  flex-shrink: 0;
  padding-right: 12px;
}

.value {
  color: #303133;
  font-size: 14px;
  flex: 1;
}

/* 特殊样式 */
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
  font-size: 14px;
}

/* 空状态样式 */
.empty-info {
  padding: 40px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .info-section {
    grid-template-columns: 1fr;
  }
  
  .label {
    width: 100px;
  }
}
</style>