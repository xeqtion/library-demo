# 图书馆管理系统

这是一个基于SpringBoot和Vue3的图书馆管理系统。

## 系统功能

### 管理员模块
- 用户管理：新增、编辑、删除、查询用户信息
- 图书管理：新增、编辑、删除、查询图书信息
- 借阅管理：审核借阅请求、管理借阅记录

### 读者模块
- 图书浏览：按分类、关键字搜索图书
- 借阅管理：借阅图书、查看借阅历史
- 个人信息管理：修改个人资料、密码等

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.x

### 前端
- Vue 3
- Element Plus
- Axios
- Vue Router

## 系统要求
- JDK 17+
- Node.js 16+
- MySQL 8.x

## 快速开始

### 数据库设置
1. 创建MySQL数据库:
   ```sql
   CREATE DATABASE library_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. 项目启动时会自动创建表结构，无需手动创建表

### 后端运行
1. 配置数据库连接(src/main/resources/application.yml)
2. 运行LibraryDemoApplication.java
3. 默认端口：8080

### 前端运行
1. 安装依赖
   ```bash
   cd manage-ui
   npm install
   ```
2. 启动开发服务器
   ```bash
   npm run dev
   ```
3. 构建生产版本
   ```bash
   npm run build
   ```

## 默认账户
- 管理员：admin/123456
- 读者：reader/123456

## API文档

### 认证相关
- POST /api/auth/login - 用户登录
- GET /api/auth/info - 获取当前用户信息

### 用户管理
- GET /api/users/page - 分页获取用户列表
- GET /api/users/{id} - 根据ID获取用户
- POST /api/users - 添加用户
- PUT /api/users - 更新用户
- DELETE /api/users/{id} - 删除用户

### 图书管理
- GET /api/books/page - 分页获取图书列表
- GET /api/books/{id} - 根据ID获取图书
- GET /api/books/categories - 获取所有图书分类
- GET /api/books/category/{category} - 根据分类获取图书
- POST /api/books - 添加图书
- PUT /api/books - 更新图书
- DELETE /api/books/{id} - 删除图书

### 借阅管理
- GET /api/borrowings/page - 分页获取借阅记录
- GET /api/borrowings/{id} - 根据ID获取借阅记录
- GET /api/borrowings/my - 获取当前用户的借阅记录
- POST /api/borrowings - 申请借阅
- PUT /api/borrowings/review/{id} - 审核借阅申请
- PUT /api/borrowings/return/{id} - 归还图书
- PUT /api/borrowings/renew/{id} - 续借图书
- DELETE /api/borrowings/{id} - 取消借阅申请 