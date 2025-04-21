<template>
  <div class="statistics-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
        <el-card shadow="hover" class="time-filter-card">
          <div class="time-filter">
            <span class="filter-label">时间范围：</span>
            <el-radio-group v-model="timeRange" @change="refreshData">
              <el-radio-button label="week">最近一周</el-radio-button>
              <el-radio-button label="month">最近一个月</el-radio-button>
              <el-radio-button label="quarter">最近三个月</el-radio-button>
              <el-radio-button label="year">最近一年</el-radio-button>
            </el-radio-group>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              @change="handleDateRangeChange"
              style="margin-left: 15px;"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>借阅趋势分析</span>
              <el-tooltip content="展示每日借阅和归还的数量趋势" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div class="chart-container" ref="borrowingTrendChart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>借阅状态分布</span>
              <el-tooltip content="展示各状态借阅记录的数量分布" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div class="chart-container" ref="borrowingStatusChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门分类统计</span>
              <el-tooltip content="展示借阅量最高的图书分类" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div class="chart-container" ref="categoryChart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户借阅排行</span>
              <el-tooltip content="展示借阅量最高的用户排行" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <div class="chart-container" ref="userRankChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <div class="card-header">
              <span>热门图书排行榜</span>
              <el-tooltip content="展示借阅次数最多的图书" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </template>
          <el-table :data="popularBooks" style="width: 100%" v-loading="loading">
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column label="图书信息" min-width="300">
              <template #default="scope">
                <div style="display: flex; align-items: center;">
                  <el-image 
                    :src="scope.row.coverImage || 'https://via.placeholder.com/60x80'" 
                    style="width: 60px; height: 80px; margin-right: 10px;"
                    fit="cover"
                  />
                  <div>
                    <div style="font-weight: bold;">《{{ scope.row.title }}》</div>
                    <div style="font-size: 13px; color: #606266;">{{ scope.row.author }}</div>
                    <div style="font-size: 13px; color: #909399;">ISBN: {{ scope.row.isbn }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="borrowCount" label="借阅次数" width="120" sortable />
            <el-table-column prop="availableCopies" label="可借数量" width="120">
              <template #default="scope">
                {{ scope.row.availableCopies }}/{{ scope.row.totalCopies }}
              </template>
            </el-table-column>
            <el-table-column prop="averageDuration" label="平均借阅时长" width="150">
              <template #default="scope">
                {{ scope.row.averageDuration }}天
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import * as echarts from 'echarts';
import { getStatisticsBorrowingTrend, getStatisticsBorrowingStatus, getStatisticsCategories, 
         getStatisticsUserRank, getStatisticsPopularBooks } from '../../api/index';

// 图表实例
let borrowingTrendChartInstance = null;
let borrowingStatusChartInstance = null;
let categoryChartInstance = null;
let userRankChartInstance = null;

// DOM引用
const borrowingTrendChart = ref(null);
const borrowingStatusChart = ref(null);
const categoryChart = ref(null);
const userRankChart = ref(null);

// 时间范围选择
const timeRange = ref('month');
const dateRange = ref([]);

// 数据加载状态
const loading = ref(false);

// 热门图书数据
const popularBooks = ref([]);

// 初始化
onMounted(async () => {
  await nextTick();
  initCharts();
  refreshData();
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', resizeCharts);
});

// 初始化所有图表
const initCharts = () => {
  // 初始化借阅趋势图表
  borrowingTrendChartInstance = echarts.init(borrowingTrendChart.value);
  
  // 初始化借阅状态分布图表
  borrowingStatusChartInstance = echarts.init(borrowingStatusChart.value);
  
  // 初始化分类统计图表
  categoryChartInstance = echarts.init(categoryChart.value);
  
  // 初始化用户排行图表
  userRankChartInstance = echarts.init(userRankChart.value);
};

// 刷新所有数据
const refreshData = async () => {
  loading.value = true;
  
  try {
    // 计算日期范围
    const { startDate, endDate } = calculateDateRange();
    
    // 获取借阅趋势数据并更新图表
    const trendData = await getStatisticsBorrowingTrend(startDate, endDate);
    updateBorrowingTrendChart(trendData);
    
    // 获取借阅状态分布数据并更新图表
    const statusData = await getStatisticsBorrowingStatus(startDate, endDate);
    updateBorrowingStatusChart(statusData);
    
    // 获取分类统计数据并更新图表
    const categoryData = await getStatisticsCategories(startDate, endDate);
    updateCategoryChart(categoryData);
    
    // 获取用户排行数据并更新图表
    const userRankData = await getStatisticsUserRank(startDate, endDate);
    updateUserRankChart(userRankData);
    
    // 获取热门图书数据
    const booksData = await getStatisticsPopularBooks(startDate, endDate);
    popularBooks.value = booksData;
  } catch (error) {
    console.error('加载统计数据失败', error);
  } finally {
    loading.value = false;
  }
};

// 根据时间范围计算日期
const calculateDateRange = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    return {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    };
  }
  
  const endDate = new Date();
  let startDate = new Date();
  
  switch (timeRange.value) {
    case 'week':
      startDate.setDate(endDate.getDate() - 7);
      break;
    case 'month':
      startDate.setMonth(endDate.getMonth() - 1);
      break;
    case 'quarter':
      startDate.setMonth(endDate.getMonth() - 3);
      break;
    case 'year':
      startDate.setFullYear(endDate.getFullYear() - 1);
      break;
  }
  
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate)
  };
};

// 日期格式化
const formatDate = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// 日期范围变化处理
const handleDateRangeChange = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    timeRange.value = 'custom';
    refreshData();
  }
};

// 更新借阅趋势图表
const updateBorrowingTrendChart = (data) => {
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['借阅数量', '归还数量']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '借阅数量',
        type: 'line',
        data: data.borrowings,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
            ]
          }
        }
      },
      {
        name: '归还数量',
        type: 'line',
        data: data.returns,
        smooth: true,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
            ]
          }
        }
      }
    ]
  };
  
  borrowingTrendChartInstance.setOption(option);
};

// 更新借阅状态分布图表
const updateBorrowingStatusChart = (data) => {
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: data.map(item => getBorrowingStatusText(item.status))
    },
    series: [
      {
        name: '借阅状态',
        type: 'pie',
        radius: '60%',
        center: ['50%', '50%'],
        data: data.map(item => ({
          name: getBorrowingStatusText(item.status),
          value: item.count,
          itemStyle: {
            color: getBorrowingStatusColor(item.status)
          }
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
  
  borrowingStatusChartInstance.setOption(option);
};

// 更新分类统计图表
const updateCategoryChart = (data) => {
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01]
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.category)
    },
    series: [
      {
        name: '借阅次数',
        type: 'bar',
        data: data.map(item => ({
          value: item.count,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          }
        }))
      }
    ]
  };
  
  categoryChartInstance.setOption(option);
};

// 更新用户排行图表
const updateUserRankChart = (data) => {
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01]
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.userName)
    },
    series: [
      {
        name: '借阅次数',
        type: 'bar',
        data: data.map(item => ({
          value: item.count,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#ff9a9e' },
              { offset: 1, color: '#fad0c4' }
            ])
          }
        }))
      }
    ]
  };
  
  userRankChartInstance.setOption(option);
};

// 获取借阅状态文本
const getBorrowingStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '借阅中';
    case 'REJECTED': return '已拒绝';
    case 'RETURNED': return '已归还';
    case 'OVERDUE': return '已逾期';
    default: return '未知状态';
  }
};

// 获取借阅状态颜色
const getBorrowingStatusColor = (status) => {
  switch (status) {
    case 'PENDING': return '#909399';
    case 'APPROVED': return '#67C23A';
    case 'REJECTED': return '#F56C6C';
    case 'RETURNED': return '#409EFF';
    case 'OVERDUE': return '#E6A23C';
    default: return '#DCDFE6';
  }
};

// 调整图表大小
const resizeCharts = () => {
  borrowingTrendChartInstance && borrowingTrendChartInstance.resize();
  borrowingStatusChartInstance && borrowingStatusChartInstance.resize();
  categoryChartInstance && categoryChartInstance.resize();
  userRankChartInstance && userRankChartInstance.resize();
};
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.time-filter-card {
  margin-bottom: 20px;
}

.time-filter {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 10px;
  font-weight: bold;
  color: #606266;
}

.chart-card {
  height: 400px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.card-header .el-icon {
  margin-left: 10px;
  color: #909399;
  cursor: pointer;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.table-card {
  margin-bottom: 20px;
}
</style> 