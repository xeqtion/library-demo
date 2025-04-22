<template>
  <div class="book-container">
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="书名/作者/ISBN" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.category" placeholder="分类" clearable>
            <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <div>
        <el-button type="primary" @click="handleAdd">添加图书</el-button>
      </div>
    </div>

    <!-- 图书表格 -->
    <el-table
      v-loading="loading"
      :data="bookList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="封面" width="100">
        <template #default="scope">
          <el-image
            :src="scope.row.coverImage || 'https://via.placeholder.com/80x100'"
            :preview-src-list="scope.row.coverImage ? [scope.row.coverImage] : []"
            fit="cover"
            style="width: 60px; height: 80px;"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" show-overflow-tooltip />
      <el-table-column prop="isbn" label="ISBN" width="120" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="120" show-overflow-tooltip />
      <el-table-column label="库存" width="140">
        <template #default="scope">
          <div>总数: {{ scope.row.totalCopies }}</div>
          <div>可借: {{ scope.row.availableCopies }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="publisher" label="出版社" show-overflow-tooltip />
      <el-table-column prop="publishYear" label="出版年份" width="100" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="primary" link @click="handleView(scope.row)">查看</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 添加/编辑图书对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="650px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="bookForm"
        :rules="rules"
        ref="bookFormRef"
        label-width="100px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="14">
            <el-form-item label="书名" prop="title">
              <el-input v-model="bookForm.title" placeholder="请输入书名" />
            </el-form-item>
            <el-form-item label="作者" prop="author">
              <el-input v-model="bookForm.author" placeholder="请输入作者" />
            </el-form-item>
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
            </el-form-item>
            <el-form-item label="分类" prop="category">
              <el-select v-model="bookForm.category" placeholder="请选择分类" style="width: 100%;">
                <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
              </el-select>
            </el-form-item>
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
            </el-form-item>
            <el-form-item label="出版年份" prop="publishYear">
              <el-input-number v-model="bookForm.publishYear" :min="1800" :max="2100" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="封面图片" prop="coverImage">
              <el-upload
                class="cover-uploader"
                action="/api/upload"
                :show-file-list="false"
                :on-success="handleCoverSuccess"
                :before-upload="beforeCoverUpload"
              >
                <img v-if="bookForm.coverImage" :src="bookForm.coverImage" class="cover-image" />
                <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">点击上传封面图片</div>
            </el-form-item>
            <el-form-item label="总数量" prop="totalCopies">
              <el-input-number v-model="bookForm.totalCopies" :min="0" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="可借数量" prop="availableCopies">
              <el-input-number v-model="bookForm.availableCopies" :min="0" :max="bookForm.totalCopies" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="bookForm.description" type="textarea" :rows="4" placeholder="请输入图书描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看图书详情对话框 -->
    <el-dialog
      title="图书详情"
      v-model="viewDialogVisible"
      width="700px"
    >
      <div class="book-detail" v-if="currentBook">
        <div class="book-detail-header">
          <div class="book-cover">
            <el-image
              :src="currentBook.coverImage || 'https://via.placeholder.com/160x200'"
              fit="cover"
              style="width: 160px; height: 200px;"
            />
          </div>
          <div class="book-info">
            <h2 class="book-title">{{ currentBook.title }}</h2>
            <div class="book-info-item"><span class="label">作者:</span> {{ currentBook.author }}</div>
            <div class="book-info-item"><span class="label">分类:</span> {{ currentBook.category }}</div>
            <div class="book-info-item"><span class="label">ISBN:</span> {{ currentBook.isbn }}</div>
            <div class="book-info-item"><span class="label">出版社:</span> {{ currentBook.publisher }}</div>
            <div class="book-info-item"><span class="label">出版年份:</span> {{ currentBook.publishYear }}</div>
            <div class="book-info-item"><span class="label">总数量:</span> {{ currentBook.totalCopies }}</div>
            <div class="book-info-item"><span class="label">可借数量:</span> {{ currentBook.availableCopies }}</div>
          </div>
        </div>
        <div class="book-description">
          <h3>图书简介</h3>
          <p>{{ currentBook.description || '暂无简介' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getBookList, getBookCategories, createBook, updateBook, deleteBook } from '../../api/index';
import { Plus } from '@element-plus/icons-vue';

// 搜索表单
const searchForm = ref({
  keyword: '',
  category: ''
});

// 表格数据
const bookList = ref([]);
const loading = ref(false);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 分类数据
const categories = ref([]);

// 对话框控制
const dialogVisible = ref(false);
const viewDialogVisible = ref(false);
const dialogTitle = ref('添加图书');
const submitting = ref(false);

// 当前操作的图书
const currentBook = ref(null);

// 图书表单
const bookForm = ref({
  id: '',
  title: '',
  author: '',
  isbn: '',
  category: '',  // 分类ID
  publisher: '', // 出版社ID
  publishYear: '',
  totalCopies: 0,
  availableCopies: 0,
  price: 0,
  coverUrl: '',
  description: ''
});

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入图书标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度应在1-100个字符之间', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 50, message: '作者名称长度应在1-50个字符之间', trigger: 'blur' }
  ],
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { pattern: /^(?:\d[- ]?){9}[\dX]$|^(?:\d[- ]?){13}$/, message: 'ISBN格式不正确', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择图书分类', trigger: 'change' }
  ],
  publisher: [
    { required: true, message: '请选择出版社', trigger: 'change' }
  ],
  totalCopies: [
    { required: true, message: '请输入总册数', trigger: 'blur' },
    { type: 'number', message: '总册数必须为数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value < 0) {
          callback(new Error('总册数不能小于0'));
        } else {
          // 当总册数变化时，检查可借册数是否合法
          if (bookForm.value.availableCopies > value) {
            bookForm.value.availableCopies = value;
          }
          callback();
        }
      }, trigger: 'blur' 
    }
  ],
  availableCopies: [
    { required: true, message: '请输入可借册数', trigger: 'blur' },
    { type: 'number', message: '可借册数必须为数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (value < 0) {
          callback(new Error('可借册数不能小于0'));
        } else if (value > bookForm.value.totalCopies) {
          callback(new Error('可借册数不能大于总册数'));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
  publishYear: [
    { required: true, message: '请输入出版年份', trigger: 'blur' },
    { type: 'number', message: '出版年份必须为数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        const currentYear = new Date().getFullYear();
        if (value < 1800 || value > currentYear) {
          callback(new Error(`出版年份必须在1800-${currentYear}之间`));
        } else {
          callback();
        }
      }, trigger: 'blur'
    }
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ]
};

// 表单引用
const bookFormRef = ref(null);

// 初始化
onMounted(async () => {
  await loadCategories();
  fetchBookList();
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
const fetchBookList = async () => {
  loading.value = true;
  try {
    const res = await getBookList(pageNum.value, pageSize.value, searchForm.value);
    console.log('获取到的图书数据:', res);
    
    // 改进数据解析逻辑，处理不同格式的响应
    if (res && res.data) {
      if (res.data.list && Array.isArray(res.data.list)) {
        bookList.value = res.data.list;
        total.value = res.data.total || 0;
      } else if (res.data.records && Array.isArray(res.data.records)) {
        bookList.value = res.data.records;
        total.value = res.data.total || 0;
      } else {
        bookList.value = [];
        total.value = 0;
      }
    } else if (res && res.list && Array.isArray(res.list)) {
      bookList.value = res.list;
      total.value = res.total || 0;
    } else if (res && res.records && Array.isArray(res.records)) {
      bookList.value = res.records;
      total.value = res.total || 0;
    } else if (res && Array.isArray(res)) {
      bookList.value = res;
      total.value = res.length;
    } else {
      console.warn('返回的图书数据格式不正确:', res);
      bookList.value = [];
      total.value = 0;
    }
    
    // 对图书列表进行去重处理
    const uniqueMap = new Map();
    bookList.value.forEach(book => {
      // 如果不存在相同ID的图书，则添加到Map中
      if (!uniqueMap.has(book.id)) {
        uniqueMap.set(book.id, book);
      }
    });
    
    // 将Map转换为数组
    bookList.value = Array.from(uniqueMap.values());
    
    // 处理封面图片默认值
    bookList.value.forEach(book => {
      if (!book.coverImage) {
        book.coverImage = 'https://via.placeholder.com/150x200/e0e0e0/808080?text=暂无封面';
      }
      // 确保数字类型字段是数字
      if (book.totalCopies && typeof book.totalCopies === 'string') {
        book.totalCopies = parseInt(book.totalCopies, 10) || 0;
      }
      if (book.availableCopies && typeof book.availableCopies === 'string') {
        book.availableCopies = parseInt(book.availableCopies, 10) || 0;
      }
      if (book.publishYear && typeof book.publishYear === 'string') {
        book.publishYear = parseInt(book.publishYear, 10) || new Date().getFullYear();
      }
    });
  } catch (error) {
    console.error('加载图书列表失败', error);
    ElMessage.error('加载图书列表失败: ' + (error.message || '请检查网络或后端服务'));
    bookList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pageNum.value = 1;
  fetchBookList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    category: ''
  };
  pageNum.value = 1;
  fetchBookList();
};

// 添加图书
const handleAdd = () => {
  dialogTitle.value = '添加图书';
  bookForm.value = {
    id: '',
    title: '',
    author: '',
    isbn: '',
    category: '',
    description: '',
    totalCopies: 1,
    availableCopies: 1,
    coverImage: '',
    publisher: '',
    publishYear: new Date().getFullYear()
  };
  dialogVisible.value = true;
};

// 编辑图书
const handleEdit = (row) => {
  dialogTitle.value = '编辑图书';
  bookForm.value = { ...row };
  dialogVisible.value = true;
};

// 查看图书详情
const handleView = (row) => {
  currentBook.value = row;
  viewDialogVisible.value = true;
};

// 删除图书
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除《${row.title}》吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteBook(row.id);
      ElMessage.success('删除成功');
      fetchBookList();
    } catch (error) {
      console.error('删除图书失败', error);
    }
  }).catch(() => {});
};

// 提交表单
const submitForm = async () => {
  if (!bookFormRef.value) return;
  
  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        if (bookForm.value.id) {
          // 编辑
          await updateBook(bookForm.value);
          ElMessage.success('编辑成功');
        } else {
          // 新增
          await createBook(bookForm.value);
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchBookList();
      } catch (error) {
        console.error('提交图书数据失败', error);
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 处理封面上传成功
const handleCoverSuccess = (res) => {
  bookForm.value.coverImage = res.data;
};

// 上传前校验
const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('上传图片只能是图片格式!');
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!');
  }
  return isImage && isLt2M;
};

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchBookList();
};

// 当前页变化
const handleCurrentChange = (val) => {
  pageNum.value = val;
  fetchBookList();
};
</script>

<style scoped>
.book-container {
  padding: 20px;
}

.search-container {
  display: flex;
  justify-content: space-between;
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

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 160px;
  height: 200px;
}

.cover-uploader:hover {
  border-color: #409EFF;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 160px;
  height: 200px;
  line-height: 200px;
  text-align: center;
}

.cover-image {
  width: 160px;
  height: 200px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 5px;
  text-align: center;
}

.book-detail {
  padding: 10px;
}

.book-detail-header {
  display: flex;
  margin-bottom: 20px;
}

.book-cover {
  margin-right: 20px;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 22px;
  font-weight: bold;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
}

.book-info-item {
  line-height: 24px;
  margin-bottom: 8px;
  color: #606266;
}

.book-info-item .label {
  color: #909399;
  margin-right: 5px;
}

.book-description {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.book-description h3 {
  font-size: 16px;
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

.book-description p {
  color: #606266;
  line-height: 1.6;
  white-space: pre-line;
}
</style> 