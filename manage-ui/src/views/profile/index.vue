<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人信息</h3>
          <el-button type="primary" @click="handleEdit">编辑</el-button>
        </div>
      </template>
      <div class="profile-content">
        <div class="avatar-container">
          <el-avatar :size="100" :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        </div>
        <div class="info-container">
          <div class="info-item">
            <span class="info-label">用户名：</span>
            <span class="info-value">{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">姓名：</span>
            <span class="info-value">{{ userInfo.name || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱：</span>
            <span class="info-value">{{ userInfo.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">角色：</span>
            <span class="info-value">{{ userInfo.role === 'ADMIN' ? '管理员' : '普通用户' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间：</span>
            <span class="info-value">{{ userInfo.createTime }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <h3>修改密码</h3>
        </div>
      </template>
      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordFormRef"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="loading">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog
      title="编辑个人信息"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        :model="editForm"
        :rules="editRules"
        ref="editFormRef"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateProfile" :loading="loading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getUserInfo, changePassword as changePasswordApi, updateUser } from '../../api/index';

// 用户信息
const userInfo = ref({});
const loading = ref(false);
const dialogVisible = ref(false);

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 修改密码表单校验规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 编辑个人信息表单
const editForm = ref({
  name: '',
  email: ''
});

// 编辑表单校验规则
const editRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
};

// 表单引用
const passwordFormRef = ref(null);
const editFormRef = ref(null);

// 初始化
onMounted(() => {
  fetchUserInfo();
});

// 获取用户信息
const fetchUserInfo = async () => {
  // 先从本地存储获取
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
  }
  
  // 然后从服务器获取最新信息
  try {
    const res = await getUserInfo();
    userInfo.value = res;
    // 更新本地存储
    localStorage.setItem('userInfo', JSON.stringify(res));
  } catch (error) {
    console.error('获取用户信息失败', error);
  }
};

// 编辑个人信息
const handleEdit = () => {
  editForm.value = {
    name: userInfo.value.name,
    email: userInfo.value.email
  };
  dialogVisible.value = true;
};

// 更新个人信息
const updateProfile = async () => {
  if (!editFormRef.value) return;
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await updateUser({
          id: userInfo.value.id,
          name: editForm.value.name,
          email: editForm.value.email
        });
        
        // 更新本地用户信息
        userInfo.value.name = editForm.value.name;
        userInfo.value.email = editForm.value.email;
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value));
        
        ElMessage.success('个人信息更新成功');
        dialogVisible.value = false;
      } catch (error) {
        console.error('更新个人信息失败', error);
      } finally {
        loading.value = false;
      }
    }
  });
};

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return;
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await changePasswordApi({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        });
        
        ElMessage.success('密码修改成功');
        // 重置表单
        passwordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        };
      } catch (error) {
        console.error('修改密码失败', error);
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.profile-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card, .password-card {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.profile-content {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.info-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  width: 100px;
  color: #909399;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-size: 14px;
}
</style> 