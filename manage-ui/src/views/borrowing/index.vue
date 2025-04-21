<template>
  <div class="borrowing-container">
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
          <div class="book-info">
            <el-image
              :src="scope.row.bookCover || 'https://via.placeholder.com/60x80'"
              :preview-src-list="scope.row.bookCover ? [scope.row.bookCover] : []"
              fit="cover"
              style="width: 45px; height: 60px; margin-right: 10px;"
            />
            <div class="book-meta">
              <div class="book-title">《{{ scope.row.bookTitle }}》</div>
              <div class="book-author">{{ scope.row.bookAuthor }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="userName" label="借阅人" width="120" />
      <el-table-column prop="borrowDate" label="借阅日期" width="180" sortable />
      <el-table-column prop="returnDate" label="应还日期" width="180" sortable />
      <el-table-column prop="actualReturnDate" label="实际归还日期" width="180" sortable>
        <template #default="scope">
          {{ scope.row.actualReturnDate || '未归还' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getBorrowingStatusType(scope.row.status)">
            {{ getBorrowingStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="renewTimes" label="续借次数" width="100" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <!-- 待审核状态 -->
          <template v-if="scope.row.status === 'PENDING'">
            <el-button type="success" link @click="handleApprove(scope.row)">批准</el-button>
            <el-button type="danger" link @click="handleReject(scope.row)">拒绝</el-button>
          </template>
          
          <!-- 已借出状态 -->
          <template v-if="scope.row.status === 'APPROVED'">
            <el-button type="primary" link @click="handleReturn(scope.row)">归还</el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleRenew(scope.row)"
              :disabled="scope.row.renewTimes >= maxRenewTimes"
            >续借</el-button>
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
          <p><strong>应还日期：</strong>{{ currentBorrowing.returnDate }}</p>
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
            <span class="detail-label">借阅人:</span>
            <span class="detail-value">{{ currentBorrowing.userName }}</span>
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
            <span class="detail-value">{{ currentBorrowing.returnDate }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">实际归还日期:</span>
            <span class="detail-value">{{ currentBorrowing.actualReturnDate || '未归还' }}</span>
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
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getBorrowingList, getBorrowingById, reviewBorrowing, returnBook, renewBook } from '../../api/index';

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
    case 'APPROVED': return '已借出';
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

// 最大续借次数
const maxRenewTimes = ref(2); // 从配置获取

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

// 初始化
onMounted(() => {
  fetchBorrowingList();
});

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
    borrowingList.value = res.list;
    total.value = res.total;
  } catch (error) {
    console.error('加载借阅列表失败', error);
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
      await returnBook(row.id);
      ElMessage.success('归还成功');
      fetchBorrowingList();
    } catch (error) {
      console.error('归还图书失败', error);
    }
  }).catch(() => {});
};

// 续借图书
const handleRenew = (row) => {
  ElMessageBox.confirm(`确认为《${row.bookTitle}》续借？`, '确认续借', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await renewBook(row.id);
      ElMessage.success('续借成功');
      fetchBorrowingList();
    } catch (error) {
      console.error('续借图书失败', error);
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
</script>

<style scoped>
.borrowing-container {
  padding: 20px;
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

.book-info {
  display: flex;
  align-items: center;
}

.book-meta {
  display: flex;
  flex-direction: column;
}

.book-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.book-author {
  font-size: 12px;
  color: #909399;
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

.history-title {
  margin-bottom: 15px;
  color: #303133;
}
</style> 