<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">图书馆管理系统</h1>
      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="账号登录" name="account">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
          >
            <el-form-item prop="username" label="用户名">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password" label="密码">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="用户注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
          >
            <el-form-item prop="username" label="用户名">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password" label="密码">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="name" label="姓名">
              <el-input
                v-model="registerForm.name"
                placeholder="请输入姓名"
                prefix-icon="UserFilled"
              />
            </el-form-item>
            <el-form-item prop="email" label="邮箱">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-button"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="管理员注册" name="adminRegister">
          <el-form
            ref="adminRegisterFormRef"
            :model="adminRegisterForm"
            :rules="adminRegisterRules"
            label-position="top"
          >
            <el-form-item prop="username" label="用户名">
              <el-input
                v-model="adminRegisterForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password" label="密码">
              <el-input
                v-model="adminRegisterForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item prop="name" label="姓名">
              <el-input
                v-model="adminRegisterForm.name"
                placeholder="请输入姓名"
                prefix-icon="UserFilled"
              />
            </el-form-item>
            <el-form-item prop="email" label="邮箱">
              <el-input
                v-model="adminRegisterForm.email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
              />
            </el-form-item>
            <el-form-item prop="adminCode" label="管理员注册码">
              <el-input
                v-model="adminRegisterForm.adminCode"
                placeholder="请输入管理员注册码"
                prefix-icon="Key"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                class="login-button"
                @click="handleAdminRegister"
              >
                注册管理员
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login, register, registerAdmin } from '../../api/index';

const router = useRouter();
const loading = ref(false);
const activeTab = ref('account');

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
});

// 登录表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
};

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  name: '',
  email: ''
});

// 注册表单验证规则
const registerRules = {
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
  ]
};

// 管理员注册表单
const adminRegisterForm = reactive({
  username: '',
  password: '',
  name: '',
  email: '',
  adminCode: ''
});

// 管理员注册表单验证规则
const adminRegisterRules = {
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
  adminCode: [
    { required: true, message: '请输入管理员注册码', trigger: 'blur' }
  ]
};

// 表单引用
const loginFormRef = ref(null);
const registerFormRef = ref(null);
const adminRegisterFormRef = ref(null);

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const res = await login(loginForm.username, loginForm.password);
        localStorage.setItem('token', res.token);
        localStorage.setItem('userInfo', JSON.stringify(res));
        ElMessage.success('登录成功');
        router.push('/');
      } catch (error) {
        ElMessage.error(error.message || '登录失败，请检查用户名和密码');
      } finally {
        loading.value = false;
      }
    }
  });
};

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await register(registerForm);
        ElMessage.success('注册成功，请登录');
        activeTab.value = 'account';
      } catch (error) {
        ElMessage.error(error.message || '注册失败');
      } finally {
        loading.value = false;
      }
    }
  });
};

// 处理管理员注册
const handleAdminRegister = async () => {
  if (!adminRegisterFormRef.value) return;
  
  await adminRegisterFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await registerAdmin(adminRegisterForm);
        ElMessage.success('管理员注册成功，请登录');
        activeTab.value = 'account';
      } catch (error) {
        ElMessage.error(error.message || '管理员注册失败');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
  background-image: url('@/assets/login-bg.jpg');
  background-size: cover;
  background-position: center;
}

.login-card {
  width: 450px;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.9);
}

.title {
  text-align: center;
  font-size: 28px;
  margin-bottom: 30px;
  color: #409EFF;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  margin-top: 10px;
  padding: 12px 0;
}
</style> 