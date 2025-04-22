import { get, post, put, del } from './index';

// 分页获取借阅记录列表
export function getBorrowingList(params) {
  return get('/borrowings/page', params);
}

// 获取借阅记录详情
export function getBorrowingById(id) {
  return get(`/borrowings/${id}`);
}

// 获取我的借阅记录
export function getMyBorrowings(params) {
  return get('/borrowings/my', params);
}

// 申请借阅
export function borrowBook(data) {
  return post('/borrowings', data);
}

// 审核借阅申请
export function reviewBorrowing(id, status, remarks) {
  return put(`/borrowings/review/${id}?status=${status}`, { remarks });
}

// 归还图书
export function returnBook(id) {
  return put(`/borrowings/return/${id}`);
}

// 续借图书
export function renewBook(id) {
  return put(`/borrowings/renew/${id}`);
}

// 取消借阅申请
export function cancelBorrowing(id) {
  return del(`/borrowings/${id}`);
} 