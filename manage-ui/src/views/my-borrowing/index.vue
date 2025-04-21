<template>
  <div class="my-borrowing-container">
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
            <span class="info-value" :class="{'overdue': item.status === 'OVERDUE'}">{{ item.returnDate }}</span>
          </div>
          <div class="info-item" v-if="item.status === 'RETURNED'">
            <span class="info-label">实际归还日期:</span>
            <span class="info-value">{{ item.actualReturnDate }}</span>
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
              {{ currentBorrowing.returnDate }}
            </span>
          </div>
          <div class="detail-item" v-if="currentBorrowing.status === 'RETURNED'">
            <span class="detail-label">实际归还日期:</span>
            <span class="detail-value">{{ currentBorrowing.actualReturnDate }}</span>
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

// 用户信息
const userInfo = ref({});

// 借阅状态
const filterStatus = ref('');
const loading = ref(false);
const borrowings = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

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
    borrowings.value = res.list;
    total.value = res.total;
  } catch (error) {
    console.error('加载借阅列表失败', error);
  } finally {
    loading.value = false;
  }
};

// 筛选状态变化
const handleFilterChange = () => {
  pageNum.value = 1;
  fetchBorrowings();
};

// 查看详情
const handleViewDetail = async (borrowing) => {
  loading.value = true;
  try {
    // 获取详细信息，包括操作历史
    const detailData = await getBorrowingById(borrowing.id);
    currentBorrowing.value = detailData;
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('获取借阅详情失败', error);
  } finally {
    loading.value = false;
  }
};

// 续借图书
const handleRenew = (borrowing) => {
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
const handleCancel = (borrowing) => {
  ElMessageBox.confirm('确认取消该借阅申请？', '取消确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelBorrowing(borrowing.id);
      ElMessage.success('借阅申请已取消');
      fetchBorrowings();
    } catch (error) {
      console.error('取消失败', error);
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