import { get, post, put, del } from './index';

// 分页获取图书列表
export function getBookList(params) {
  return get('/books/page', params);
}

// 获取图书详情
export function getBookById(id) {
  return get(`/books/${id}`);
}

// 添加图书
export function addBook(data) {
  return post('/books', data);
}

// 更新图书
export function updateBook(data) {
  return put('/books', data);
}

// 删除图书
export function deleteBook(id) {
  return del(`/books/${id}`);
}

// 获取图书分类列表
export function getCategoryList() {
  return get('/books/categories');
}

// 根据分类获取图书列表
export function getBooksByCategory(category, params) {
  return get(`/books/category/${category}`, params);
} 