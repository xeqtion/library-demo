<template>
  <div class="borrowing-container">
    <!-- 统计信息卡片 -->
    <div class="stat-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-card-content">
              <div class="stat-icon pending-icon">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">待审核</div>
                <div class="stat-value">{{ statistics.pending || 0 }}</div>
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
                <div class="stat-value">{{ statistics.borrowed || 0 }}</div>
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
                <div class="stat-value">{{ statistics.overdue || 0 }}</div>
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
                <div class="stat-value">{{ statistics.returned || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="书名/用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="借阅状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已借出" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已归还" value="RETURNED" />
            <el-option label="已逾期" value="OVERDUE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="借阅开始日期"
            end-placeholder="借阅结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 借阅表格 -->
    <el-table
      v-loading="loading"
      :data="borrowingList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="bookTitle" label="图书" min-width="180" show-overflow-tooltip>
        <template #default="scope">
              <div class="book-title">《{{ scope.row.bookTitle }}》</div>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="借阅人" width="120" />
      <el-table-column prop="borrowDate" label="借阅日期" width="180" sortable />
      <el-table-column prop="dueDate" label="应还日期" width="180" sortable />
      <el-table-column label="实际归还日期" width="180" sortable>
        <template #default="scope">
          {{ scope.row.returnDate || '未归还' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getBorrowingStatusType(scope.row.status)">
            {{ getBorrowingStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <!-- 待审核状态 -->
          <template v-if="scope.row.status === 'PENDING'">
            <el-button type="success" link @click="handleApprove(scope.row)">批准</el-button>
            <el-button type="danger" link @click="handleReject(scope.row)">拒绝</el-button>
          </template>
          
          <!-- 已借出状态 -->
          <template v-if="scope.row.status === 'APPROVED'">
            <el-button type="primary" link @click="handleReturn(scope.row)">归还</el-button>
          </template>
          
          <!-- 已逾期状态 -->
          <template v-if="scope.row.status === 'OVERDUE'">
            <el-button type="warning" link @click="handleReturn(scope.row)">逾期归还</el-button>
          </template>
          
          <!-- 所有状态都显示详情 -->
          <el-button type="primary" link @click="handleViewDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      :title="reviewDialogTitle"
      v-model="reviewDialogVisible"
      width="500px"
    >
      <div class="review-info">
        <p>您正在{{ reviewType === 'approve' ? '批准' : '拒绝' }}以下借阅申请：</p>
        <div class="review-book-info" v-if="currentBorrowing">
          <p><strong>图书：</strong>《{{ currentBorrowing.bookTitle }}》</p>
          <p><strong>借阅人：</strong>{{ currentBorrowing.userName }}</p>
          <p><strong>借阅日期：</strong>{{ currentBorrowing.borrowDate }}</p>
          <p><strong>应还日期：</strong>{{ currentBorrowing.dueDate }}</p>
        </div>
      </div>
      <el-form :model="reviewForm" ref="reviewFormRef">
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="reviewForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button :type="reviewType === 'approve' ? 'success' : 'danger'" @click="submitReview" :loading="submitting">
            {{ reviewType === 'approve' ? '批准借阅' : '拒绝借阅' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 借阅详情对话框 -->
    <el-dialog
      title="借阅详情"
      v-model="detailDialogVisible"
      width="700px"
    >
      <div class="borrowing-detail" v-if="currentBorrowing">
        <div class="detail-header">
          <div class="detail-book">
            <img 
              :src="currentBorrowing.bookCover || 'https://via.placeholder.com/120x160'"
              class="book-cover"
            />
            <div class="detail-book-info">
              <h3>{{ currentBorrowing.bookTitle }}</h3>
              <p><span class="info-label">借阅ID:</span> {{ currentBorrowing.id }}</p>
              <p><span class="info-label">借阅人:</span> {{ currentBorrowing.userName }}</p>
              <p><span class="info-label">状态:</span> 
                <el-tag :type="getBorrowingStatusType(currentBorrowing.status)">
                  {{ getBorrowingStatusText(currentBorrowing.status) }}
                </el-tag>
              </p>
            </div>
          </div>
        </div>
        <el-divider />
        <div class="detail-info">
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
          <div class="detail-item" v-if="currentBorrowing.returnDate">
            <span class="detail-label">实际归还日期:</span>
            <span class="detail-value">{{ currentBorrowing.returnDate }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">备注:</span>
            <span class="detail-value">{{ currentBorrowing.remarks || '无' }}</span>
          </div>
        </div>
        
        <div class="detail-actions" v-if="currentBorrowing.status !== 'RETURNED'">
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
          
          <!-- 待审核状态 -->
          <template v-if="currentBorrowing.status === 'PENDING'">
            <el-button type="success" @click="handleApproveFromDetail">批准借阅</el-button>
            <el-button type="danger" @click="handleRejectFromDetail">拒绝借阅</el-button>
          </template>
          
          <!-- 已借出状态 -->
          <template v-if="currentBorrowing.status === 'APPROVED' || currentBorrowing.status === 'BORROWED'">
            <el-button type="warning" @click="handleReturnFromDetail">归还图书</el-button>
          </template>
          
          <!-- 已逾期状态 -->
          <template v-if="currentBorrowing.status === 'OVERDUE'">
            <el-button type="danger" @click="handleReturnFromDetail">逾期归还</el-button>
          </template>
        </div>
        <div class="detail-actions" v-else>
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getBorrowingList, getBorrowingById, reviewBorrowing, returnBook } from '../../api/index';
import { Timer, Reading, Warning, Select, InfoFilled } from '@element-plus/icons-vue';

// 借阅状态类型和文本
const getBorrowingStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'info';
    case 'APPROVED': return 'success';
    case 'BORROWED': return 'success';
    case 'REJECTED': return 'danger';
    case 'RETURNED': return '';
    case 'OVERDUE': return 'warning';
    default: return '';
  }
};

const getBorrowingStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '已借出';
    case 'BORROWED': return '已借出';
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
    default: return '';
  }
};

// 搜索表单
const searchForm = ref({
  keyword: '',
  status: '',
  dateRange: []
});

// 表格数据
const borrowingList = ref([]);
const loading = ref(false);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 当前操作的借阅
const currentBorrowing = ref(null);

// 审核对话框
const reviewDialogVisible = ref(false);
const reviewDialogTitle = ref('');
const reviewType = ref('approve'); // approve 或 reject
const reviewForm = ref({
  borrowingId: '',
  approved: true,
  remarks: ''
});
const reviewFormRef = ref(null);
const submitting = ref(false);

// 详情对话框
const detailDialogVisible = ref(false);

// 统计数据
const statistics = ref({
  pending: 0,
  borrowed: 0,
  overdue: 0,
  returned: 0
});

// 初始化
onMounted(() => {
  fetchBorrowingList();
});

// 计算统计数据
const calculateStatistics = () => {
  const stats = {
    pending: 0,
    borrowed: 0,
    overdue: 0,
    returned: 0,
    rejected: 0,
    total: borrowingList.value.length
  };
  
  borrowingList.value.forEach(item => {
    if (item.status === 'PENDING') stats.pending++;
    else if (item.status === 'BORROWED' || item.status === 'APPROVED') stats.borrowed++;
    else if (item.status === 'OVERDUE') stats.overdue++;
    else if (item.status === 'RETURNED') stats.returned++;
    else if (item.status === 'REJECTED') stats.rejected++;
  });

  statistics.value = stats;
};

// 获取借阅列表
const fetchBorrowingList = async () => {
  loading.value = true;
  try {
    // 处理日期范围
    const params = { ...searchForm.value };
    if (params.dateRange && params.dateRange.length === 2) {
      params.startDate = params.dateRange[0];
      params.endDate = params.dateRange[1];
    }
    delete params.dateRange;
    
    const res = await getBorrowingList(pageNum.value, pageSize.value, params);
    
    // 改进数据解析逻辑，处理不同格式的响应
    if (res && res.records) {
      borrowingList.value = res.records;
      total.value = res.total || 0;
      // 如果后端返回了pageNum和pageSize，则使用后端的值
      if (res.pageNum) pageNum.value = res.pageNum;
      if (res.pageSize) pageSize.value = res.pageSize;
    } else if (res && res.list) {
    borrowingList.value = res.list;
      total.value = res.total || 0;
    } else if (Array.isArray(res)) {
      borrowingList.value = res;
      total.value = res.length;
    } else {
      borrowingList.value = [];
      total.value = 0;
      console.warn('无法识别的借阅数据格式:', res);
    }
    
    // 计算统计数据
    calculateStatistics();
    
  } catch (error) {
    console.error('加载借阅列表失败', error);
    ElMessage.error('加载借阅列表失败: ' + error.message);
    borrowingList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pageNum.value = 1;
  fetchBorrowingList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    status: '',
    dateRange: []
  };
  pageNum.value = 1;
  fetchBorrowingList();
};

// 批准借阅
const handleApprove = (row) => {
  currentBorrowing.value = row;
  reviewType.value = 'approve';
  reviewDialogTitle.value = '批准借阅';
  reviewForm.value = {
    borrowingId: row.id,
    approved: true,
    remarks: ''
  };
  reviewDialogVisible.value = true;
};

// 拒绝借阅
const handleReject = (row) => {
  currentBorrowing.value = row;
  reviewType.value = 'reject';
  reviewDialogTitle.value = '拒绝借阅';
  reviewForm.value = {
    borrowingId: row.id,
    approved: false,
    remarks: ''
  };
  reviewDialogVisible.value = true;
};

// 提交审核
const submitReview = async () => {
  submitting.value = true;
  try {
    await reviewBorrowing(
      reviewForm.value.borrowingId,
      reviewForm.value.approved,
      reviewForm.value.remarks
    );
    ElMessage.success(`已${reviewForm.value.approved ? '批准' : '拒绝'}借阅`);
    reviewDialogVisible.value = false;
    fetchBorrowingList();
  } catch (error) {
    console.error('审核借阅失败', error);
    
    // 提供更详细的错误信息
    let errorMsg = '审核借阅失败';
    if (error.message) {
      if (error.message.includes('available') || error.message.includes('可用数量')) {
        errorMsg = '图书可用数量不足，无法批准借阅';
      } else if (error.message.includes('not found') || error.message.includes('不存在')) {
        errorMsg = '借阅记录不存在或已被处理';
      } else {
        errorMsg = `审核借阅失败: ${error.message}`;
      }
    }
    
    ElMessage.error(errorMsg);
  } finally {
    submitting.value = false;
  }
};

// 归还图书
const handleReturn = (row) => {
  ElMessageBox.confirm(`确认归还《${row.bookTitle}》？`, '确认归还', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 检查并映射状态
      if (row.status === 'APPROVED' || row.status === 'BORROWED') {
        console.log('状态为已借出，准备归还');
        row.status = 'BORROWED';
      }
      
      await returnBook(row.id);
      ElMessage.success('归还成功');
      fetchBorrowingList();
    } catch (error) {
      console.error('归还图书失败', error);
      let errorMsg = '归还图书失败';
      if (error.message) {
        if (error.message.includes('只能归还已借出或逾期的图书')) {
          errorMsg = '该图书当前状态不允许归还，请确认图书状态是否为"已借出"或"已逾期"';
        } else {
          errorMsg = `归还图书失败: ${error.message}`;
        }
      }
      ElMessage.error(errorMsg);
    }
  }).catch(() => {});
};

// 查看详情
const handleViewDetail = async (row) => {
  loading.value = true;
  try {
    // 获取详细信息，包括操作历史
    const detailData = await getBorrowingById(row.id);
    currentBorrowing.value = detailData;
    
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('获取借阅详情失败', error);
  } finally {
    loading.value = false;
  }
};

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchBorrowingList();
};

// 当前页变化
const handleCurrentChange = (val) => {
  pageNum.value = val;
  fetchBorrowingList();
};

// 从详情对话框批准借阅
const handleApproveFromDetail = () => {
  if (!currentBorrowing.value) return;
  
  reviewForm.value = {
    borrowingId: currentBorrowing.value.id,
    approved: true,
    remarks: ''
  };
  
  ElMessageBox.confirm(`确认批准《${currentBorrowing.value.bookTitle}》的借阅申请？`, '批准确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await reviewBorrowing(
        reviewForm.value.borrowingId,
        reviewForm.value.approved,
        reviewForm.value.remarks
      );
      ElMessage.success('已批准借阅申请');
      detailDialogVisible.value = false;
      fetchBorrowingList();
    } catch (error) {
      console.error('审核借阅失败', error);
      ElMessage.error('审核借阅失败: ' + (error.message || '未知错误'));
    }
  }).catch(() => {});
};

// 从详情对话框拒绝借阅
const handleRejectFromDetail = () => {
  if (!currentBorrowing.value) return;
  
  ElMessageBox.prompt('请输入拒绝原因', '拒绝借阅', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入拒绝原因（可选）',
    type: 'warning'
  }).then(async ({ value }) => {
    try {
      await reviewBorrowing(
        currentBorrowing.value.id,
        false,
        value || '管理员拒绝'
      );
      ElMessage.success('已拒绝借阅申请');
      detailDialogVisible.value = false;
      fetchBorrowingList();
    } catch (error) {
      console.error('审核借阅失败', error);
      ElMessage.error('审核借阅失败: ' + (error.message || '未知错误'));
    }
  }).catch(() => {});
};

// 从详情对话框归还图书
const handleReturnFromDetail = () => {
  if (!currentBorrowing.value) return;
  
  ElMessageBox.confirm(`确认归还《${currentBorrowing.value.bookTitle}》？`, '归还确认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 先检查状态
      if (currentBorrowing.value.status !== 'BORROWED' && currentBorrowing.value.status !== 'OVERDUE') {
        const mappedStatus = mapStatusForReturn(currentBorrowing.value.status);
        if (mappedStatus !== 'BORROWED' && mappedStatus !== 'OVERDUE') {
          ElMessage.warning(`当前图书状态为"${getBorrowingStatusText(currentBorrowing.value.status)}"，系统将尝试归还`);
        }
      }
      
      await returnBook(currentBorrowing.value.id);
      ElMessage.success('归还成功');
      detailDialogVisible.value = false;
      fetchBorrowingList();
    } catch (error) {
      console.error('归还图书失败', error);
      let errorMsg = '归还图书失败';
      if (error.message) {
        if (error.message.includes('只能归还已借出或逾期的图书')) {
          errorMsg = '该图书状态不允许归还，请确认图书状态正确';
        } else {
          errorMsg = `归还图书失败: ${error.message}`;
        }
      }
      ElMessage.error(errorMsg);
    }
  }).catch(() => {});
};
</script>

<style scoped>
.borrowing-container {
  padding: 20px;
}

.stat-container {
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

.stat-icon i {
  font-size: 24px;
  color: white;
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

.search-container {
  margin-bottom: 20px;
  background-color: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.book-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.review-info {
  margin-bottom: 20px;
}

.review-book-info {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
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

.detail-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 15px;
  margin-bottom: 20px;
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
  font-weight: bold;
}

.book-cover {
  width: 120px;
  height: 160px;
  margin-right: 20px;
  object-fit: cover; /* 确保图片保持比例 */
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.info-label {
  font-weight: bold;
  margin-right: 5px;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 