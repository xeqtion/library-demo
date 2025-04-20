import { get, post, put, del } from './index';

// 用户登录
export function login(data) {
  return post('/auth/login', data);
}

// 获取当前用户信息
export function getUserInfo() {
  return get('/auth/info');
}

// 退出登录
export function logout() {
  return post('/auth/logout');
}

// 分页获取用户列表
export function getUserList(params) {
  return get('/users/page', params);
}

// 获取用户详情
export function getUserById(id) {
  return get(`/users/${id}`);
}

// 添加用户
export function addUser(data) {
  return post('/users', data);
}

// 更新用户
export function updateUser(data) {
  return put('/users', data);
}

// 删除用户
export function deleteUser(id) {
  return del(`/users/${id}`);
}

// 修改密码
export function changePassword(data) {
  return post('/users/change-password', data);
} 