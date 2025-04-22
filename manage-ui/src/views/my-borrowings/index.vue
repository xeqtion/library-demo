<template>
  <div class="my-borrowing-container">
    <h2>我的借阅记录</h2>
    
    <!-- 添加统计概览卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon pending-icon">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">待审核</div>
                <div class="stat-value">{{ borrowingsStats.pending || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon borrowed-icon">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">借阅中</div>
                <div class="stat-value">{{ borrowingsStats.borrowed || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon overdue-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">已逾期</div>
                <div class="stat-value">{{ borrowingsStats.overdue || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon returned-icon">
                <el-icon><Select /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">已归还</div>
                <div class="stat-value">{{ borrowingsStats.returned || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="filter-container">
      <el-radio-group v-model="filterStatus" @change="handleFilterChange" size="large">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="PENDING">待审核</el-radio-button>
        <el-radio-button label="APPROVED">借阅中</el-radio-button>
        <el-radio-button label="OVERDUE">已逾期</el-radio-button>
        <el-radio-button label="RETURNED">已归还</el-radio-button>
        <el-radio-button label="REJECTED">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="loading" class="borrowing-list">
      <el-empty v-if="borrowings.length === 0" description="暂无借阅记录" />
      
      <!-- 这里会显示借阅列表 -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Timer, Reading, Warning, Select, InfoFilled, Clock, Close } from '@element-plus/icons-vue';

// 定义状态
const loading = ref(false);
const borrowings = ref([]);
const filterStatus = ref('');
const borrowingsStats = ref({
  pending: 0,
  borrowed: 0,
  overdue: 0,
  returned: 0,
  rejected: 0,
  total: 0
});

// 筛选变化处理
const handleFilterChange = () => {
  loading.value = true;
  
  // 模拟加载数据
  setTimeout(() => {
    loading.value = false;
    ElMessage.success('筛选条件已更新');
  }, 500);
};

// 组件挂载时
onMounted(() => {
  // 模拟加载数据
  loading.value = true;
  
  setTimeout(() => {
    borrowingsStats.value = {
      pending: 2,
      borrowed: 3,
      overdue: 1,
      returned: 5,
      rejected: 0,
      total: 11
    };
    
    loading.value = false;
    ElMessage.success('数据加载成功');
  }, 1000);
});
</script>

<style scoped>
.my-borrowing-container {
  padding: 20px;
}

.stats-container {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-card-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.pending-icon {
  background-color: #e6f7ff;
  color: #1890ff;
}

.borrowed-icon {
  background-color: #f6ffed;
  color: #52c41a;
}

.overdue-icon {
  background-color: #fff2e8;
  color: #fa8c16;
}

.returned-icon {
  background-color: #f9f0ff;
  color: #722ed1;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-top: 5px;
}

.filter-container {
  margin-bottom: 20px;
}

.borrowing-list {
  min-height: 300px;
}
</style> 