<template>
  <div class="my-borrowing-container">
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

      <el-card v-for="item in borrowings" :key="item.id" class="borrowing-item" shadow="hover">
        <div class="borrowing-header">
          <div class="borrowing-book">
            <el-image
              :src="item.bookCover || 'https://via.placeholder.com/80x100'"
              fit="cover"
              style="width: 80px; height: 100px; margin-right: 15px;"
            />
            <div class="borrowing-book-info">
              <h3 class="book-title">《{{ item.bookTitle }}》</h3>
              <p class="book-author">{{ item.bookAuthor }}</p>
              <p class="book-isbn">ISBN: {{ item.bookIsbn }}</p>
              <el-tag size="small" class="book-category">{{ item.bookCategory }}</el-tag>
            </div>
          </div>
          <div class="borrowing-status">
            <el-tag :type="getBorrowingStatusType(item.status)">
              {{ getBorrowingStatusText(item.status) }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="borrowing-info">
          <div class="info-item">
            <span class="info-label">借阅日期:</span>
            <span class="info-value">{{ item.borrowDate }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">应还日期:</span>
            <span class="info-value" :class="{'overdue': item.status === 'OVERDUE'}">{{ item.dueDate }}</span>
          </div>
          <div class="info-item" v-if="item.status === 'RETURNED'">
            <span class="info-label">实际归还日期:</span>
            <span class="info-value">{{ item.returnDate }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">续借次数:</span>
            <span class="info-value">{{ item.renewTimes }} 次</span>
          </div>
          <div class="info-item" v-if="item.remarks">
            <span class="info-label">备注:</span>
            <span class="info-value">{{ item.remarks }}</span>
          </div>
        </div>

        <div class="borrowing-actions">
          <!-- 借阅中状态 -->
          <template v-if="item.status === 'APPROVED'">
            <el-tooltip content="查看借阅详情" placement="top">
              <el-button type="info" circle @click="handleViewDetail(item)">
                <el-icon><InfoFilled /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="申请续借" placement="top">
              <el-button
                type="primary"
                circle
                @click="handleRenew(item)"
                :disabled="item.renewTimes >= maxRenewTimes"
              >
                <el-icon><TimeoutFilled /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="归还图书" placement="top">
              <el-button type="success" circle @click="handleReturn(item)">
                <el-icon><Select /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
          
          <!-- 已逾期状态 -->
          <template v-else-if="item.status === 'OVERDUE'">
            <el-tooltip content="查看借阅详情" placement="top">
              <el-button type="info" circle @click="handleViewDetail(item)">
                <el-icon><InfoFilled /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="逾期归还" placement="top">
              <el-button type="warning" circle @click="handleReturn(item)">
                <el-icon><Select /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
          
          <!-- 待审核状态 -->
          <template v-else-if="item.status === 'PENDING'">
            <el-tooltip content="查看借阅详情" placement="top">
              <el-button type="info" circle @click="handleViewDetail(item)">
                <el-icon><InfoFilled /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="取消申请" placement="top">
              <el-button type="danger" circle @click="handleCancel(item)">
                <el-icon><Close /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
          
          <!-- 其他状态 -->
          <template v-else>
            <el-tooltip content="查看借阅详情" placement="top">
              <el-button type="info" circle @click="handleViewDetail(item)">
                <el-icon><InfoFilled /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
        </div>
      </el-card>
    </div>

    <div class="pagination-container" v-if="total > 0">
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 借阅详情对话框 -->
    <el-dialog
      title="借阅详情"
      v-model="detailDialogVisible"
      width="700px"
    >
      <div class="borrowing-detail" v-if="currentBorrowing">
        <div class="detail-header">
          <div class="detail-book">
            <el-image
              :src="currentBorrowing.bookCover || 'https://via.placeholder.com/120x160'"
              fit="cover"
              style="width: 120px; height: 160px; margin-right: 20px;"
            />
            <div class="detail-book-info">
              <h3>《{{ currentBorrowing.bookTitle }}》</h3>
              <p>作者: {{ currentBorrowing.bookAuthor }}</p>
              <p>ISBN: {{ currentBorrowing.bookIsbn }}</p>
              <p>分类: {{ currentBorrowing.bookCategory }}</p>
            </div>
          </div>
        </div>
        <el-divider />
        <div class="detail-content">
          <div class="detail-item">
            <span class="detail-label">借阅ID:</span>
            <span class="detail-value">{{ currentBorrowing.id }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">借阅状态:</span>
            <span class="detail-value">
              <el-tag :type="getBorrowingStatusType(currentBorrowing.status)">
                {{ getBorrowingStatusText(currentBorrowing.status) }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">借阅日期:</span>
            <span class="detail-value">{{ currentBorrowing.borrowDate }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">应还日期:</span>
            <span class="detail-value" :class="{'overdue': currentBorrowing.status === 'OVERDUE'}">
              {{ currentBorrowing.dueDate }}
            </span>
          </div>
          <div class="detail-item" v-if="currentBorrowing.status === 'RETURNED'">
            <span class="detail-label">实际归还日期:</span>
            <span class="detail-value">{{ currentBorrowing.returnDate }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">续借次数:</span>
            <span class="detail-value">{{ currentBorrowing.renewTimes }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">备注:</span>
            <span class="detail-value">{{ currentBorrowing.remarks || '无' }}</span>
          </div>
        </div>
        <el-divider />
        <div class="detail-history" v-if="currentBorrowing.historyRecords && currentBorrowing.historyRecords.length > 0">
          <h3 class="history-title">操作记录</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(record, index) in currentBorrowing.historyRecords"
              :key="index"
              :timestamp="record.operateTime"
              :type="getOperationTypeColor(record.operationType)"
            >
              {{ getOperationTypeText(record.operationType) }}：{{ record.remarks || '无备注' }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getMyBorrowings, getBorrowingById, renewBook, returnBook, cancelBorrowing } from '../../api/index';
import { Timer, Reading, Warning, Select, InfoFilled, TimeoutFilled, Close } from '@element-plus/icons-vue';

// 用户信息
const userInfo = ref({});

// 借阅状态
const filterStatus = ref('');
const loading = ref(false);
const borrowings = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 借阅统计
const borrowingsStats = ref({
  pending: 0,
  borrowed: 0,
  overdue: 0,
  returned: 0,
  rejected: 0,
  total: 0
});

// 最大续借次数
const maxRenewTimes = ref(2); // 从配置获取

// 当前操作的借阅
const currentBorrowing = ref(null);
const detailDialogVisible = ref(false);

// 借阅状态类型和文本
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

const getBorrowingStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '借阅中';
    case 'REJECTED': return '已拒绝';
    case 'RETURNED': return '已归还';
    case 'OVERDUE': return '已逾期';
    default: return '';
  }
};

// 操作类型颜色和文本
const getOperationTypeColor = (type) => {
  switch (type) {
    case 'BORROW': return 'primary';
    case 'APPROVE': return 'success';
    case 'REJECT': return 'danger';
    case 'RETURN': return 'info';
    case 'RENEW': return 'warning';
    case 'CANCEL': return 'info';
    default: return '';
  }
};

const getOperationTypeText = (type) => {
  switch (type) {
    case 'BORROW': return '申请借阅';
    case 'APPROVE': return '批准借阅';
    case 'REJECT': return '拒绝借阅';
    case 'RETURN': return '归还图书';
    case 'RENEW': return '续借图书';
    case 'CANCEL': return '取消申请';
    default: return '';
  }
};

// 初始化
onMounted(() => {
  // 获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
    fetchBorrowings();
  }
});

// 获取我的借阅列表
const fetchBorrowings = async () => {
  loading.value = true;
  try {
    const res = await getMyBorrowings(pageNum.value, pageSize.value, filterStatus.value);
    
    // 处理不同格式的返回数据
    if (res && res.records) {
      borrowings.value = res.records;
      total.value = res.total || 0;
    } else if (res && res.list) {
      borrowings.value = res.list;
      total.value = res.total || 0;
    } else if (Array.isArray(res)) {
      borrowings.value = res;
      total.value = res.length;
    } else {
      borrowings.value = [];
      total.value = 0;
    }
    
    // 字段修正
    borrowings.value.forEach(item => {
      // 处理字段名不匹配问题
      if (!item.dueDate && item.returnDate) {
        item.dueDate = item.returnDate;
      }
      
      // 处理续借次数
      if (item.renewTimes === undefined && item.remarks) {
        item.renewTimes = extractRenewTimesFromRemarks(item.remarks);
      } else if (item.renewTimes === undefined) {
        item.renewTimes = 0;
      }
    });
    
    // 更新借阅统计数据
    calculateBorrowingsStats();
  } catch (error) {
    console.error('加载借阅列表失败', error);
    ElMessage.error('加载借阅列表失败: ' + (error.message || '未知错误'));
    borrowings.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 筛选状态变化
const handleFilterChange = () => {
  pageNum.value = 1;
  fetchBorrowings();
};

// 从备注中提取续借次数
const extractRenewTimesFromRemarks = (remarks) => {
  if (!remarks) return 0;
  
  const match = remarks.match(/续借次数:\s*(\d+)/);
  if (match && match[1]) {
    return parseInt(match[1], 10);
  }
  return 0;
};

// 计算借阅统计
const calculateBorrowingsStats = () => {
  // 获取所有借阅记录（包括当前页以外的）
  getMyBorrowings(1, 1000, '')
    .then(res => {
      let allBorrowings = [];
      
      if (res && res.records) {
        allBorrowings = res.records;
      } else if (res && res.list) {
        allBorrowings = res.list;
      } else if (Array.isArray(res)) {
        allBorrowings = res;
      }
      
      // 重置统计
      const stats = {
        pending: 0,
        borrowed: 0,
        overdue: 0,
        returned: 0,
        rejected: 0,
        total: allBorrowings.length
      };
      
      // 计数
      allBorrowings.forEach(item => {
        if (item.status === 'PENDING') stats.pending++;
        else if (item.status === 'APPROVED') stats.borrowed++;
        else if (item.status === 'OVERDUE') stats.overdue++;
        else if (item.status === 'RETURNED') stats.returned++;
        else if (item.status === 'REJECTED') stats.rejected++;
      });
      
      borrowingsStats.value = stats;
    })
    .catch(error => {
      console.error('获取借阅统计失败', error);
    });
};

// 查看详情
const handleViewDetail = async (borrowing) => {
  loading.value = true;
  try {
    // 获取详细信息，包括操作历史
    const detailData = await getBorrowingById(borrowing.id);
    currentBorrowing.value = detailData;
    
    // 字段修正
    if (!currentBorrowing.value.dueDate && currentBorrowing.value.returnDate) {
      currentBorrowing.value.dueDate = currentBorrowing.value.returnDate;
    }
    
    // 处理续借次数
    if (currentBorrowing.value.renewTimes === undefined && currentBorrowing.value.remarks) {
      currentBorrowing.value.renewTimes = extractRenewTimesFromRemarks(currentBorrowing.value.remarks);
    } else if (currentBorrowing.value.renewTimes === undefined) {
      currentBorrowing.value.renewTimes = 0;
    }
    
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('获取借阅详情失败', error);
    ElMessage.error('获取借阅详情失败: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 续借图书
const handleRenew = (borrowing) => {
  // 检查续借次数
  if (borrowing.renewTimes >= maxRenewTimes.value) {
    ElMessage.warning(`该图书已达到最大续借次数（${maxRenewTimes.value}次）`);
    return;
  }
  
  ElMessageBox.confirm('确认续借该图书？', '续借确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await renewBook(borrowing.id);
      ElMessage.success('续借申请已提交');
      fetchBorrowings();
    } catch (error) {
      console.error('续借失败', error);
      ElMessage.error('续借失败: ' + (error.message || '未知错误'));
    }
  }).catch(() => {});
};

// 归还图书
const handleReturn = (borrowing) => {
  ElMessageBox.confirm('确认归还该图书？', '归还确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await returnBook(borrowing.id);
      ElMessage.success('图书归还成功');
      fetchBorrowings();
    } catch (error) {
      console.error('归还失败', error);
    }
  }).catch(() => {});
};

// 取消借阅申请
const handleCancel = (item) => {
  ElMessageBox.confirm(`确认取消《${item.bookTitle}》的借阅申请？`, '取消确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelBorrowing(item.id);
      ElMessage.success('借阅申请已取消');
      fetchBorrowings(); // 刷新列表
    } catch (error) {
      console.error('取消借阅失败', error);
      ElMessage.error('取消借阅失败: ' + (error.message || '未知错误'));
    }
  }).catch(() => {});
};

// 分页处理
const handleCurrentChange = (val) => {
  pageNum.value = val;
  fetchBorrowings();
};
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
  background-color: #909399;
}

.borrowed-icon {
  background-color: #409EFF;
}

.overdue-icon {
  background-color: #F56C6C;
}

.returned-icon {
  background-color: #67C23A;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.filter-container {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
}

.borrowing-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.borrowing-item {
  transition: all 0.3s;
}

.borrowing-item:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.borrowing-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.borrowing-book {
  display: flex;
}

.borrowing-book-info {
  display: flex;
  flex-direction: column;
}

.book-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.book-author, .book-isbn {
  margin: 5px 0;
  font-size: 14px;
  color: #606266;
}

.book-category {
  margin-top: 5px;
  align-self: flex-start;
}

.borrowing-info {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px 20px;
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  color: #909399;
  width: 100px;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 14px;
}

.info-value.overdue {
  color: #F56C6C;
  font-weight: bold;
}

.borrowing-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.borrowing-detail {
  padding: 10px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-book {
  display: flex;
}

.detail-book-info h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.detail-book-info p {
  margin: 5px 0;
  color: #606266;
}

.detail-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 15px;
}

.detail-item {
  display: flex;
  align-items: center;
}

.detail-label {
  color: #909399;
  width: 100px;
}

.detail-value {
  color: #303133;
  font-weight: 500;
}

.detail-value.overdue {
  color: #F56C6C;
}

.history-title {
  margin-bottom: 15px;
  color: #303133;
}
</style> 