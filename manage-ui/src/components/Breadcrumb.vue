<template>
  <el-breadcrumb separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
        <span v-if="index === breadcrumbs.length - 1 || item.redirect === 'noredirect'" class="no-link">{{ item.meta.title }}</span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const breadcrumbs = ref([]);

// 根据当前路由生成面包屑数据
const getBreadcrumb = () => {
  // 过滤没有meta或没有title的路由
  let matched = route.matched.filter(
    item => item.meta && item.meta.title
  );
  
  // 第一个是首页，如果不是则添加首页
  const first = matched[0];
  if (first && first.path !== '/dashboard') {
    matched = [
      {
        path: '/dashboard',
        meta: { title: '首页' }
      }
    ].concat(matched);
  }
  
  breadcrumbs.value = matched;
};

// 路由切换时更新面包屑
watch(
  () => route.path,
  () => {
    getBreadcrumb();
  },
  { immediate: true }
);

// 处理链接点击
const handleLink = (item) => {
  const { redirect, path } = item;
  if (redirect) {
    router.push(redirect);
    return;
  }
  router.push(path);
};
</script>

<style scoped>
.no-link {
  color: #97a8be;
  cursor: text;
}

.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-leave-active {
  position: absolute;
}
</style> 