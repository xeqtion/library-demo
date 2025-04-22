<template>
  <div class="user-container">
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/邮箱" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.role" placeholder="角色" clearable>
            <el-option label="用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <div>
        <el-button type="primary" @click="handleAdd">添加用户</el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <el-table
      v-loading="loading"
      :data="userList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="80">
        <template #default="scope">
          <el-avatar 
            :size="40" 
            :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
          />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'">
            {{ scope.row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-switch
            v-model="scope.row.enabled"
            :active-value="true"
            :inactive-value="false"
            @change="handleStatusChange(scope.row)"
            :disabled="scope.row.role === 'ADMIN' && scope.row.id === 1"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            link 
            @click="handleEdit(scope.row)"
            :disabled="scope.row.role === 'ADMIN' && scope.row.id === 1 && userInfo.id !== 1"
          >编辑</el-button>
          <el-button 
            type="primary" 
            link 
            @click="handleResetPassword(scope.row.id)"
          >重置密码</el-button>
          <el-button 
            type="danger" 
            link 
            @click="handleDelete(scope.row)"
            :disabled="scope.row.role === 'ADMIN' && scope.row.id === 1"
          >删除</el-button>
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

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="userForm"
        :rules="userRules"
        ref="userFormRef"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="userForm.id !== ''"/>
        </el-form-item>
        <el-form-item v-if="!userForm.id" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="userForm.enabled" :active-value="true" :inactive-value="false" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPwdVisible"
      width="400px"
    >
      <el-form
        :model="resetPwdForm"
        :rules="resetPwdRules"
        ref="resetPwdFormRef"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetPwdVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPwd" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getUserList, createUser, updateUser, deleteUser, resetUserPassword, getUserById, updateUserStatus } from '../../api/index';

// 当前登录用户信息
const userInfo = ref({});

// 搜索表单
const searchForm = ref({
  keyword: '',
  role: ''
});

// 表格数据
const userList = ref([]);
const loading = ref(false);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

// 对话框控制
const dialogVisible = ref(false);
const dialogTitle = ref('添加用户');
const submitting = ref(false);

// 重置密码对话框
const resetPwdVisible = ref(false);
const resetPwdForm = ref({
  userId: '',
  password: '',
  confirmPassword: ''
});
const resetPwdRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPwdForm.value.password) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 用户表单
const userForm = ref({
  id: '',
  username: '',
  password: '',
  name: '',
  email: '',
  role: 'USER',
  enabled: true
});

// 表单校验规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
};

// 表单引用
const userFormRef = ref(null);
const resetPwdFormRef = ref(null);

// 初始化
onMounted(() => {
  // 获取当前用户信息
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
  }
  fetchUserList();
});

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    const res = await getUserList(pageNum.value, pageSize.value, searchForm.value);
    console.log('获取到的用户数据:', res);
    
    // 改进数据解析逻辑
    if (res && res.data) {
      // 处理嵌套在data中的情况
      if (res.data.list && Array.isArray(res.data.list)) {
        userList.value = res.data.list;
        total.value = res.data.total || 0;
      } else if (res.data.records && Array.isArray(res.data.records)) {
        userList.value = res.data.records;
        total.value = res.data.total || 0;
      } else {
        userList.value = [];
        total.value = 0;
      }
    } else if (res && res.list && Array.isArray(res.list)) {
      // 直接在res中的情况
      userList.value = res.list;
      total.value = res.total || 0;
    } else if (res && res.records && Array.isArray(res.records)) {
      // 另一种常见格式
      userList.value = res.records;
      total.value = res.total || 0;
    } else if (res && Array.isArray(res)) {
      // 直接返回了数组的情况
      userList.value = res;
      total.value = res.length;
    } else {
      console.warn('返回的数据格式不正确:', res);
      userList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('加载用户列表失败', error);
    ElMessage.error('加载用户列表失败: ' + (error.message || '请检查网络或后端服务'));
    
    // 移除测试数据加载，避免掩盖真实问题
    userList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pageNum.value = 1;
  fetchUserList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    role: ''
  };
  pageNum.value = 1;
  fetchUserList();
};

// 添加用户
const handleAdd = () => {
  dialogTitle.value = '添加用户';
  userForm.value = {
    id: '',
    username: '',
    password: '',
    name: '',
    email: '',
    role: 'USER',
    enabled: true
  };
  dialogVisible.value = true;
};

// 编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户';
  userForm.value = {
    id: row.id,
    username: row.username,
    name: row.name,
    email: row.email,
    role: row.role,
    enabled: row.enabled
  };
  dialogVisible.value = true;
};

// 重置密码
const handleResetPassword = (userId) => {
  resetPwdForm.value = {
    userId: userId,
    password: '',
    confirmPassword: ''
  };
  resetPwdVisible.value = true;
};

// 提交重置密码
const submitResetPwd = async () => {
  if (!resetPwdFormRef.value) return;
  
  await resetPwdFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        await resetUserPassword(resetPwdForm.value.userId, resetPwdForm.value.password);
        ElMessage.success('密码重置成功');
        resetPwdVisible.value = false;
      } catch (error) {
        console.error('重置密码失败', error);
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 提交用户表单
const submitForm = async () => {
  if (!userFormRef.value) return;
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        if (userForm.value.id) {
          // 编辑
          await updateUser(userForm.value);
          ElMessage.success('编辑成功');
        } else {
          // 新增
          await createUser(userForm.value);
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchUserList();
      } catch (error) {
        console.error('提交用户数据失败', error);
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 修改用户状态
const handleStatusChange = async (row) => {
  try {
    // 使用专门的状态更新API
    await updateUserStatus(row.id, row.enabled);
    ElMessage.success(`已${row.enabled ? '启用' : '禁用'}该用户`);
  } catch (error) {
    // 还原状态
    row.enabled = !row.enabled;
    console.error('修改用户状态失败', error);
    ElMessage.error('修改用户状态失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户"${row.name || row.username}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id);
      ElMessage.success('删除成功');
      fetchUserList();
    } catch (error) {
      // 显示详细的错误信息
      const errorMsg = error.response?.data?.message || '删除用户失败';
      ElMessage.error(errorMsg);
      console.error('删除用户失败', error);
    }
  }).catch(() => {});
};

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val;
  fetchUserList();
};

// 当前页变化
const handleCurrentChange = (val) => {
  pageNum.value = val;
  fetchUserList();
};
</script>

<style scoped>
.user-container {
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
</style> 