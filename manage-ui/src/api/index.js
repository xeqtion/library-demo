import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const instance = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
});

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 添加token到请求头
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  response => {
    // 仅返回数据部分
    return response.data.data;
  },
  error => {
    // 处理错误响应
    let message = '请求失败';
    if (error.response) {
      // 服务器响应了但状态码不是2xx
      const status = error.response.status;
      const data = error.response.data;
      
      // 处理不同状态码
      if (status === 401) {
        message = '未授权，请重新登录';
        // 清除token并跳转到登录页
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        window.location.href = '/login';
      } else if (status === 403) {
        message = '权限不足，无法访问';
      } else if (status === 404) {
        message = '请求的资源不存在';
      } else if (status === 500) {
        message = '服务器错误';
      }
      
      // 使用服务器返回的错误消息
      if (data && data.message) {
        message = data.message;
      }
    } else if (error.request) {
      // 请求发出但没有收到响应
      message = '服务器无响应，请检查网络';
    }
    
    // 显示错误消息
    ElMessage.error(message);
    
    return Promise.reject(error);
  }
);

// 用户认证相关API
export const login = (username, password) => {
  return instance.post('/auth/login', { username, password });
};

export const register = (userData) => {
  return instance.post('/auth/register', userData);
};

export const registerAdmin = (adminData) => {
  return instance.post('/auth/register/admin', adminData);
};

export const getUserInfo = () => {
  return instance.get('/auth/info');
};

export const changePassword = (passwordData) => {
  return instance.post('/auth/change-password', passwordData);
};

// 用户管理API
export const getUserList = (pageNum, pageSize, query) => {
  return instance.get('/users/page', { params: { pageNum, pageSize, ...query } });
};

export const getUserById = (id) => {
  return instance.get(`/users/${id}`);
};

export const createUser = (userData) => {
  return instance.post('/users', userData);
};

export const updateUser = (userData) => {
  return instance.put('/users', userData);
};

export const deleteUser = (id) => {
  return instance.delete(`/users/${id}`);
};

export const resetUserPassword = (userId, password) => {
  return instance.post(`/users/${userId}/reset-password`, { password });
};

// 图书管理API
export const getBookList = (pageNum, pageSize, query) => {
  return instance.get('/books/page', { params: { pageNum, pageSize, ...query } });
};

export const getBookById = (id) => {
  return instance.get(`/books/${id}`);
};

export const getBookCategories = () => {
  return instance.get('/books/categories');
};

export const getBooksByCategory = (category, pageNum, pageSize) => {
  return instance.get(`/books/category/${category}`, { params: { pageNum, pageSize } });
};

export const createBook = (bookData) => {
  return instance.post('/books', bookData);
};

export const updateBook = (bookData) => {
  return instance.put('/books', bookData);
};

export const deleteBook = (id) => {
  return instance.delete(`/books/${id}`);
};

// 借阅管理API
export const getBorrowingList = (pageNum, pageSize, query) => {
  return instance.get('/borrowings/page', { params: { pageNum, pageSize, ...query } });
};

export const getBorrowingById = (id) => {
  return instance.get(`/borrowings/${id}`);
};

export const getMyBorrowings = (pageNum, pageSize, status) => {
  // 从localStorage获取userInfo
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  const userId = userInfo.id;
  
  const params = { pageNum, pageSize, userId };
  if (status) {
    params.status = status;
  }
  return instance.get('/borrowings/my', { params });
};

export const createBorrowing = (borrowingData) => {
  return instance.post('/borrowings', borrowingData);
};

export const reviewBorrowing = (id, approved, remarks) => {
  return instance.put(`/borrowings/review/${id}`, { approved, remarks });
};

export const returnBook = (id) => {
  return instance.put(`/borrowings/return/${id}`);
};

export const renewBook = (id) => {
  return instance.put(`/borrowings/renew/${id}`);
};

export const cancelBorrowing = (id) => {
  return instance.delete(`/borrowings/${id}`);
};

// 仪表盘API
export const getDashboardData = () => {
  return instance.get('/dashboard/stats');
};

export const getRecentBorrowings = () => {
  return instance.get('/dashboard/recent-borrowings');
};

export const getPopularBooks = () => {
  return instance.get('/dashboard/popular-books');
};

// 统计分析API
export const getStatisticsBorrowingTrend = (startDate, endDate) => {
  return instance.get('/statistics/borrowing-trend', { 
    params: { startDate, endDate } 
  });
};

export const getStatisticsBorrowingStatus = (startDate, endDate) => {
  return instance.get('/statistics/borrowing-status', { 
    params: { startDate, endDate } 
  });
};

export const getStatisticsCategories = (startDate, endDate) => {
  return instance.get('/statistics/categories', { 
    params: { startDate, endDate } 
  });
};

export const getStatisticsUserRank = (startDate, endDate) => {
  return instance.get('/statistics/user-rank', { 
    params: { startDate, endDate } 
  });
};

export const getStatisticsPopularBooks = (startDate, endDate) => {
  return instance.get('/statistics/popular-books', { 
    params: { startDate, endDate } 
  });
};

export default instance; 