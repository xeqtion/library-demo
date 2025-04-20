import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const service = axios.create({
  baseURL: '/api',
  timeout: 10000
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在请求头中添加token
    const userInfo = localStorage.getItem('userInfo');
    if (userInfo) {
      const user = JSON.parse(userInfo);
      if (user.token) {
        config.headers['Authorization'] = 'Bearer ' + user.token;
      }
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data;
    // 如果响应码不是200，则判断为错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '未知错误');
      
      // 401: 未登录，跳转到登录页
      if (res.code === 401) {
        localStorage.removeItem('userInfo');
        window.location.href = '/login';
      }
      
      return Promise.reject(new Error(res.message || '未知错误'));
    } else {
      return res;
    }
  },
  error => {
    ElMessage.error(error.message || '网络错误');
    return Promise.reject(error);
  }
);

// 封装GET请求
export function get(url, params) {
  return service({
    url,
    method: 'get',
    params
  });
}

// 封装POST请求
export function post(url, data) {
  return service({
    url,
    method: 'post',
    data
  });
}

// 封装PUT请求
export function put(url, data) {
  return service({
    url,
    method: 'put',
    data
  });
}

// 封装DELETE请求
export function del(url) {
  return service({
    url,
    method: 'delete'
  });
}

export default service; 