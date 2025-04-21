<template>
  <div class="app-wrapper">
    <!-- 侧边栏导航 -->
    <div class="sidebar-container" :class="{ 'is-collapse': isCollapse }">
      <div class="logo">
        <span v-if="!isCollapse">图书馆管理系统</span>
        <span v-else>📚</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          
          <!-- 管理员菜单 -->
          <template v-if="userInfo.role === 'ADMIN'">
            <el-menu-item index="/users">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
            
            <el-menu-item index="/books">
              <el-icon><Reading /></el-icon>
              <template #title>图书管理</template>
            </el-menu-item>
            
            <el-menu-item index="/borrowings">
              <el-icon><Document /></el-icon>
              <template #title>借阅管理</template>
            </el-menu-item>
          </template>
          
          <!-- 所有用户菜单 -->
          <el-menu-item index="/library">
            <el-icon><Collection /></el-icon>
            <template #title>图书浏览</template>
          </el-menu-item>
          
          <el-menu-item index="/my-borrowings">
            <el-icon><Tickets /></el-icon>
            <template #title>我的借阅</template>
          </el-menu-item>
          
          <el-menu-item index="/profile">
            <el-icon><UserFilled /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="left-menu">
          <el-icon class="toggle-sidebar" @click="toggleSidebar">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <breadcrumb />
        </div>
        <div class="right-menu">
          <el-dropdown trigger="click">
            <div class="avatar-container">
              <el-avatar :size="36" :src="userInfo.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <span class="username">{{ userInfo.name || userInfo.username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/profile')">
                  <el-icon><UserFilled /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import Breadcrumb from '../../components/Breadcrumb.vue';

const router = useRouter();
const isCollapse = ref(false);
const userInfo = ref({});

// 获取当前激活的菜单项
const activeMenu = computed(() => {
  return router.currentRoute.value.path;
});

// 切换侧边栏展开/折叠
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value;
};

// 获取用户信息
onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
  } else {
    router.push('/login');
  }
});

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确认要退出登录吗?', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    ElMessage.success('退出成功');
    router.push('/login');
  }).catch(() => {});
};
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.sidebar-container {
  width: 210px;
  height: 100%;
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar-container.is-collapse {
  width: 64px;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #263445;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.navbar {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.07);
  background-color: #fff;
}

.left-menu {
  display: flex;
  align-items: center;
}

.toggle-sidebar {
  font-size: 20px;
  margin-right: 15px;
  cursor: pointer;
}

.right-menu {
  display: flex;
  align-items: center;
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  font-size: 14px;
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

/* 过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from,
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style> 