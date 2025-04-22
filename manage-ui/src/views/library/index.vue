<template>
  <div class="library-container">
    <div class="filter-container">
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索书名、作者或ISBN"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="category-box">
        <el-tag
          :effect="selectedCategory === '' ? 'dark' : 'plain'"
          @click="handleCategorySelect('')"
          style="cursor: pointer; margin-right: 8px;"
        >
          全部
        </el-tag>
        <el-tag
          v-for="category in categories"
          :key="category"
          :effect="selectedCategory === category ? 'dark' : 'plain'"
          @click="handleCategorySelect(category)"
          style="cursor: pointer; margin-right: 8px; margin-bottom: 8px;"
        >
          {{ category }}
        </el-tag>
      </div>
    </div>

    <div v-loading="loading" class="book-grid">
      <el-empty v-if="books.length === 0" description="暂无图书" />
      <div
        v-for="book in books"
        :key="book.id"
        class="book-card"
        @click="handleViewDetail(book)"
      >
        <div class="book-cover">
          <el-image
            :src="book.coverImage"
            fit="cover"
            :lazy="true"
            loading="lazy"
          />
          <div class="book-status" :class="{ 'available': book.availableCopies > 0 }">
            {{ book.availableCopies > 0 ? '可借阅' : '已借完' }}
          </div>
        </div>
        <div class="book-info">
          <div class="book-title" :title="book.title">{{ book.title }}</div>
          <div class="book-author">{{ book.author }}</div>
          <div class="book-meta">
            <el-tag size="small" effect="plain">{{ book.category }}</el-tag>
            <span class="book-copies">库存: {{ book.availableCopies }}/{{ book.totalCopies }}</span>
          </div>
          <div class="book-actions">
            <el-button
              type="primary"
              size="small"
              @click.stop="handleBorrow(book)"
              :disabled="book.availableCopies <= 0"
            >
              借阅
            </el-button>
            <el-button
              type="info"
              plain
              size="small"
              @click.stop="handleViewDetail(book)"
            >
              详情
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-container">
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 借阅表单对话框 -->
    <el-dialog
      title="借阅图书"
      v-model="borrowDialogVisible"
      width="500px"
    >
      <div class="borrow-book-info" v-if="currentBook">
        <div class="borrow-book-header">
          <el-image
            :src="currentBook.coverImage || 'https://via.placeholder.com/100x150'"
            fit="cover"
            style="width: 100px; height: 150px; margin-right: 15px;"
          />
          <div class="borrow-book-details">
            <h3>《{{ currentBook.title }}》</h3>
            <p>作者: {{ currentBook.author }}</p>
            <p>出版社: {{ currentBook.publisher }}</p>
            <p>可借数量: {{ currentBook.availableCopies }}</p>
          </div>
        </div>
        <el-divider />
        <el-form
          :model="borrowForm"
          :rules="borrowRules"
          ref="borrowFormRef"
          label-width="100px"
          label-position="right"
        >
          <el-form-item label="借阅天数" prop="borrowDays">
            <el-select v-model="borrowForm.borrowDays" placeholder="请选择借阅天数" style="width: 100%">
              <el-option label="7天" :value="7" />
              <el-option label="14天" :value="14" />
              <el-option label="30天" :value="30" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remarks">
            <el-input
              v-model="borrowForm.remarks"
              type="textarea"
              :rows="3"
              placeholder="请输入借阅备注信息（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="borrowDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBorrow" :loading="submitting">确认借阅</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 图书详情对话框 -->
    <el-dialog
      title="图书详情"
      v-model="detailDialogVisible"
      width="700px"
    >
      <div class="book-detail" v-if="currentBook">
        <div class="detail-header">
          <div class="detail-cover">
            <el-image
              :src="currentBook.coverImage || 'https://via.placeholder.com/180x240'"
              fit="cover"
              style="width: 180px; height: 240px; margin-right: 20px;"
            />
          </div>
          <div class="detail-info">
            <h2 class="detail-title">{{ currentBook.title }}</h2>
            <p class="detail-item"><span class="detail-label">作者:</span> {{ currentBook.author }}</p>
            <p class="detail-item"><span class="detail-label">分类:</span> {{ currentBook.category }}</p>
            <p class="detail-item"><span class="detail-label">ISBN:</span> {{ currentBook.isbn }}</p>
            <p class="detail-item"><span class="detail-label">出版社:</span> {{ currentBook.publisher }}</p>
            <p class="detail-item"><span class="detail-label">出版年份:</span> {{ currentBook.publishYear }}</p>
            <p class="detail-item">
              <span class="detail-label">馆藏状态:</span> 
              <el-tag :type="currentBook.availableCopies > 0 ? 'success' : 'danger'" size="small">
                {{ currentBook.availableCopies > 0 ? '可借阅' : '已借完' }}
              </el-tag>
            </p>
            <p class="detail-item">
              <span class="detail-label">可借数量:</span> {{ currentBook.availableCopies }}/{{ currentBook.totalCopies }}
            </p>
          </div>
        </div>
        <el-divider />
        <div class="detail-description">
          <h3 class="detail-section-title">图书简介</h3>
          <p class="detail-description-content">{{ currentBook.description || '暂无简介' }}</p>
        </div>
        <div class="detail-actions" v-if="currentBook.availableCopies > 0">
          <el-button type="primary" @click="handleBorrow(currentBook)">借阅此书</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getBookList, getBookCategories, getBooksByCategory, createBorrowing } from '../../api/index';

// 用户信息
const userInfo = ref({});

// 搜索和过滤
const searchQuery = ref('');
const selectedCategory = ref('');
const categories = ref([]);

// 表格数据
const books = ref([]);
const loading = ref(false);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(12);

// 当前操作的图书
const currentBook = ref(null);

// 对话框控制
const borrowDialogVisible = ref(false);
const detailDialogVisible = ref(false);
const submitting = ref(false);

// 借阅表单
const borrowForm = ref({
  bookId: '',
  borrowDays: 14,
  remarks: ''
});

// 表单校验规则
const borrowRules = {
  borrowDays: [
    { required: true, message: '请选择借阅天数', trigger: 'change' }
  ]
};

// 表单引用
const borrowFormRef = ref(null);

// 初始化
onMounted(async () => {
  // 获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
  }
  
  await loadCategories();
  fetchBooks();
});

// 加载分类数据
const loadCategories = async () => {
  try {
    const res = await getBookCategories();
    categories.value = res;
  } catch (error) {
    console.error('加载分类数据失败', error);
  }
};

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true;
  try {
    let res;
    
    if (selectedCategory.value) {
      // 按分类获取图书
      res = await getBooksByCategory(selectedCategory.value, pageNum.value, pageSize.value);
    } else {
      // 获取所有图书或搜索
      const params = {
        keyword: searchQuery.value
      };
      res = await getBookList(pageNum.value, pageSize.value, params);
    }
    
    console.log('获取到的图书数据:', res);
    
    // 改进数据解析逻辑，处理不同格式的响应
    if (res && res.data) {
      if (res.data.list && Array.isArray(res.data.list)) {
        books.value = res.data.list;
        total.value = res.data.total || 0;
      } else if (res.data.records && Array.isArray(res.data.records)) {
        books.value = res.data.records;
        total.value = res.data.total || 0;
      } else {
        books.value = [];
        total.value = 0;
      }
    } else if (res && res.list && Array.isArray(res.list)) {
      books.value = res.list;
      total.value = res.total || 0;
    } else if (res && res.records && Array.isArray(res.records)) {
      books.value = res.records;
      total.value = res.total || 0;
    } else if (res && Array.isArray(res)) {
      books.value = res;
      total.value = res.length;
    } else {
      console.warn('返回的图书数据格式不正确:', res);
      books.value = [];
      total.value = 0;
    }
    
    // 处理封面图片默认值
    books.value.forEach(book => {
      if (!book.coverImage) {
        book.coverImage = 'https://via.placeholder.com/150x200/e0e0e0/808080?text=暂无封面';
      }
    });
  } catch (error) {
    console.error('加载图书列表失败', error);
    ElMessage.error('加载图书列表失败: ' + (error.message || '请检查网络或后端服务'));
    books.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索图书
const handleSearch = () => {
  selectedCategory.value = ''; // 清除分类筛选
  pageNum.value = 1;
  fetchBooks();
};

// 选择分类
const handleCategorySelect = (category) => {
  selectedCategory.value = category;
  pageNum.value = 1;
  fetchBooks();
};

// 分页变化
const handleCurrentChange = (val) => {
  pageNum.value = val;
  fetchBooks();
};

// 借阅图书
const handleBorrow = (book) => {
  if (!userInfo.value.id) {
    ElMessage.warning('请先登录');
    return;
  }
  
  currentBook.value = book;
  borrowForm.value = {
    bookId: book.id,
    borrowDays: 14,
    remarks: ''
  };
  borrowDialogVisible.value = true;
};

// 提交借阅申请
const submitBorrow = async () => {
  if (!borrowFormRef.value) return;
  
  await borrowFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        // 添加用户ID
        const borrowData = {
          ...borrowForm.value,
          userId: userInfo.value.id
        };
        
        await createBorrowing(borrowData);
        ElMessage.success('借阅申请已提交，请等待管理员审核');
        borrowDialogVisible.value = false;
        fetchBooks(); // 刷新图书列表
      } catch (error) {
        console.error('提交借阅申请失败', error);
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 查看图书详情
const handleViewDetail = (book) => {
  currentBook.value = book;
  detailDialogVisible.value = true;
};
</script>

<style scoped>
.library-container {
  padding: 20px;
}

.filter-container {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.search-box {
  margin-bottom: 15px;
  max-width: 500px;
}

.category-box {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 25px;
  margin-bottom: 20px;
}

.book-card {
  width: 100%;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 8px;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
}

.book-cover {
  position: relative;
  height: 240px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.book-cover .el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.book-status {
  position: absolute;
  top: 10px;
  right: 0;
  background-color: #F56C6C;
  color: white;
  padding: 4px 10px;
  font-size: 12px;
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

.book-status.available {
  background-color: #67C23A;
}

.book-info {
  padding: 14px;
}

.book-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  height: 2.8em;
}

.book-author {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.book-copies {
  font-size: 12px;
  color: #909399;
}

.book-actions {
  display: flex;
  justify-content: space-between;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.borrow-book-header {
  display: flex;
  margin-bottom: 20px;
}

.borrow-book-details h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.borrow-book-details p {
  margin: 5px 0;
  color: #606266;
}

.detail-header {
  display: flex;
  margin-bottom: 20px;
}

.detail-title {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 22px;
  color: #303133;
}

.detail-item {
  margin-bottom: 8px;
  line-height: 1.6;
}

.detail-label {
  font-weight: bold;
  color: #606266;
  margin-right: 5px;
}

.detail-section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
}

.detail-description-content {
  color: #606266;
  line-height: 1.8;
  white-space: pre-line;
}

.detail-actions {
  margin-top: 20px;
}
</style> 