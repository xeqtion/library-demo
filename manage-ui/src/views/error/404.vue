<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="not-found-title">404</h1>
      <div class="not-found-desc">抱歉，您访问的页面不存在</div>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
// 404页面不需要特殊的脚本逻辑
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.not-found-content {
  text-align: center;
}

.not-found-title {
  font-size: 120px;
  color: #409EFF;
  margin: 0;
  font-weight: bold;
  line-height: 1.2;
  text-shadow: 4px 4px 10px rgba(0, 0, 0, 0.1);
}

.not-found-desc {
  font-size: 20px;
  color: #606266;
  margin: 20px 0 30px;
}
</style> 