<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <img src="@/assets/library.svg" class="welcome-icon" />
            <div class="welcome-info">
              <h2>欢迎使用图书馆管理系统</h2>
              <p>{{ currentTime }}</p>
              <p>{{ greeting }}, {{ userInfo.name || userInfo.username }}!</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="card-row">
      <!-- 数据统计卡片 -->
      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">总图书数</span>
            <el-icon class="data-icon book-icon"><Reading /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">{{ dashboardData.totalBooks }}</div>
            <div class="data-footer">
              <span class="data-label">可借阅</span>
              <span class="data-sublabel">{{ dashboardData.availableBooks }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">注册用户</span>
            <el-icon class="data-icon user-icon"><User /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">{{ dashboardData.totalUsers }}</div>
            <div class="data-footer">
              <span class="data-label">今日新增</span>
              <span class="data-sublabel">{{ dashboardData.newUsers }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">借阅总数</span>
            <el-icon class="data-icon borrow-icon"><Tickets /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">{{ dashboardData.totalBorrowings }}</div>
            <div class="data-footer">
              <span class="data-label">本月借阅</span>
              <span class="data-sublabel">{{ dashboardData.monthlyBorrowings }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
        <el-card shadow="hover" class="data-card">
          <div class="data-header">
            <span class="data-title">待处理</span>
            <el-icon class="data-icon pending-icon"><Document /></el-icon>
          </div>
          <div class="data-content">
            <div class="data-value">{{ dashboardData.pendingRequests }}</div>
            <div class="data-footer">
              <span class="data-label">逾期未还</span>
              <span class="data-sublabel">{{ dashboardData.overdueBooks }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="card-row">
      <!-- 最近借阅列表 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <el-card shadow="hover" class="list-card">
          <template #header>
            <div class="card-header">
              <div class="title">
                <i class="el-icon-reading"></i>
                最近借阅
              </div>
              <el-button v-if="userInfo.role === 'ADMIN'" type="primary" link @click="$router.push('/borrowings')">查看全部</el-button>
              <el-button v-else type="primary" link @click="$router.push('/my-borrowings')">查看我的借阅</el-button>
            </div>
          </template>
          <el-table :data="recentBorrowings" style="width: 100%" :show-header="false" height="330">
            <el-table-column>
              <template #default="scope">
                <div class="borrowing-item">
                  <div class="borrowing-info">
                    <div class="borrowing-title" :title="scope.row.bookTitle">《{{ scope.row.bookTitle }}》</div>
                    <div class="borrowing-details">
                      <span>借阅人: {{ scope.row.userName }}</span>
                      <span>借阅日期: {{ formatDate(scope.row.borrowDate) }}</span>
                      <el-tag :type="getBorrowingStatusType(scope.row.status)" size="small">{{ getBorrowingStatusText(scope.row.status) }}</el-tag>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="recentBorrowings.length === 0" class="empty-data">
            暂无借阅记录
          </div>
        </el-card>
      </el-col>

      <!-- 热门图书列表 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="12">
        <el-card shadow="hover" class="list-card">
          <template #header>
            <div class="card-header">
              <div class="title">
                <i class="el-icon-collection"></i>
                热门图书
              </div>
              <el-button type="primary" link @click="$router.push('/library')">浏览全部</el-button>
            </div>
          </template>
          <el-table :data="popularBooks" style="width: 100%" :show-header="false" height="330">
            <el-table-column>
              <template #default="scope">
                <div class="book-item">
                  <div class="book-cover">
                    <el-image :src="scope.row.coverImage || 'https://via.placeholder.com/80x100'" fit="cover"></el-image>
                  </div>
                  <div class="book-info">
                    <div class="book-title" :title="scope.row.title">《{{ scope.row.title }}》</div>
                    <div class="book-author" :title="scope.row.author">{{ scope.row.author }}</div>
                    <div class="book-details">
                      <el-tag size="small">{{ scope.row.category }}</el-tag>
                      <span>借阅: {{ scope.row.borrowCount }}次</span>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="popularBooks.length === 0" class="empty-data">
            暂无图书数据
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 仪表盘添加底部卡片，提供统计入口 -->
    <el-row :gutter="20" class="card-row" v-if="userInfo.role === 'ADMIN'">
      <el-col :span="24">
        <el-card shadow="hover" class="data-analysis-card">
          <div class="data-analysis-content">
            <div class="data-analysis-info">
              <h3>数据统计分析</h3>
              <p>查看更详细的借阅趋势、分类统计、用户排行等数据分析</p>
            </div>
            <div class="data-analysis-action">
              <el-button type="primary" @click="$router.push('/statistics')">
                <el-icon><DataAnalysis /></el-icon>
                前往统计分析
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getDashboardData, getRecentBorrowings, getPopularBooks } from '../../api/index';
import { Reading, User, Tickets, Document, DataAnalysis } from '@element-plus/icons-vue';

// 用户信息
const userInfo = ref({});

// 仪表盘数据
const dashboardData = ref({
  totalBooks: 0,
  availableBooks: 0,
  totalUsers: 0,
  newUsers: 0,
  totalBorrowings: 0,
  monthlyBorrowings: 0,
  pendingRequests: 0,
  overdueBooks: 0
});

// 最近借阅记录
const recentBorrowings = ref([]);

// 热门图书
const popularBooks = ref([]);

// 当前时间
const currentTime = computed(() => {
  const now = new Date();
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  });
});

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) {
    return '凌晨好';
  } else if (hour < 9) {
    return '早上好';
  } else if (hour < 12) {
    return '上午好';
  } else if (hour < 14) {
    return '中午好';
  } else if (hour < 17) {
    return '下午好';
  } else if (hour < 19) {
    return '傍晚好';
  } else {
    return '晚上好';
  }
});

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
};

// 获取借阅状态类型
const getBorrowingStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'info';
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'danger';
    case 'RETURNED': return '';
    case 'OVERDUE': return 'warning';
    default: return '';
  }
};

// 获取借阅状态文本
const getBorrowingStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '已借出';
    case 'REJECTED': return '已拒绝';
    case 'RETURNED': return '已归还';
    case 'OVERDUE': return '已逾期';
    default: return '';
  }
};

// 初始化数据
onMounted(async () => {
  // 获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
  }

  try {
    // 获取仪表盘数据
    const dashboardResponse = await getDashboardData();
    dashboardData.value = dashboardResponse;

    // 获取最近借阅记录
    const borrowingsResponse = await getRecentBorrowings();
    recentBorrowings.value = borrowingsResponse;

    // 获取热门图书
    const booksResponse = await getPopularBooks();
    popularBooks.value = booksResponse;
  } catch (error) {
    console.error('加载仪表盘数据失败', error);
  }
});
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.card-row {
  margin-top: 20px;
}

.welcome-card {
  margin-bottom: 10px;
}

.welcome-content {
  display: flex;
  align-items: center;
}

.welcome-icon {
  width: 70px;
  height: 70px;
  margin-right: 20px;
}

.welcome-info h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.welcome-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.data-card {
  height: 160px;
  overflow: hidden;
  padding-bottom: 15px;
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-top: 5px;
}

.data-title {
  font-size: 16px;
  color: #606266;
}

.data-icon {
  font-size: 28px;
  padding: 8px;
  border-radius: 50%;
}

.book-icon {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
}

.user-icon {
  color: #67C23A;
  background-color: rgba(103, 194, 58, 0.1);
}

.borrow-icon {
  color: #E6A23C;
  background-color: rgba(230, 162, 60, 0.1);
}

.pending-icon {
  color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.1);
}

.data-content {
  display: flex;
  flex-direction: column;
  height: 100px;
}

.data-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.data-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.data-label {
  font-size: 13px;
  color: #909399;
}

.data-sublabel {
  font-size: 13px;
  color: #606266;
  font-weight: bold;
}

.list-card {
  margin-bottom: 20px;
  height: 450px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.el-icon-reading {
  color: #409EFF;
  margin-right: 5px;
  font-size: 18px;
}

.el-icon-collection {
  color: #67C23A;
  margin-right: 5px;
  font-size: 18px;
}

.borrowing-item {
  padding: 8px 0;
  border-bottom: 1px solid #EBEEF5;
}

.borrowing-title {
  font-size: 14px;
  color: #303133;
  font-weight: bold;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.borrowing-details {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.book-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #EBEEF5;
}

.book-cover {
  min-width: 45px;
  width: 45px;
  height: 65px;
  margin-right: 10px;
  overflow: hidden;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 14px;
  color: #303133;
  font-weight: bold;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-author {
  font-size: 13px;
  color: #606266;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-details {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.empty-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  color: #909399;
  font-size: 14px;
}

.data-analysis-card {
  margin-bottom: 20px;
}

.data-analysis-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.data-analysis-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.data-analysis-info p {
  margin: 0;
  color: #606266;
}

.data-analysis-action {
  margin-left: 20px;
}

/* 添加表格容器样式 */
.el-table {
  flex: 1;
  overflow-y: auto;
}
</style> 