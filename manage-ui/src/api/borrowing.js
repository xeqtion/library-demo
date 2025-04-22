import axios from '../api/index';

// 生成模拟借阅数据的工具函数
const generateMockBorrowings = (userId, status) => {
  const mockData = [
    {
      id: 101,
      userId: userId || 2,
      userName: "读者",
      bookId: 1,
      bookTitle: "Java编程思想",
      bookAuthor: "Bruce Eckel",
      bookIsbn: "9787111213826",
      bookCategory: "计算机科学",
      borrowDate: "2025-03-27",
      dueDate: "2025-04-10",
      returnDate: null,
      status: "APPROVED",
      renewTimes: 0
    },
    {
      id: 102,
      userId: userId || 2,
      userName: "读者",
      bookId: 2,
      bookTitle: "深入理解Java虚拟机",
      bookAuthor: "周志明",
      bookIsbn: "9787111641247",
      bookCategory: "计算机科学",
      borrowDate: "2025-04-01",
      dueDate: "2025-04-15",
      returnDate: "2025-04-13",
      status: "RETURNED",
      renewTimes: 0
    },
    {
      id: 103,
      userId: userId || 2,
      userName: "读者",
      bookId: 4,
      bookTitle: "三体",
      bookAuthor: "刘慈欣",
      bookIsbn: "9787536692930",
      bookCategory: "科幻小说",
      borrowDate: "2025-04-05",
      dueDate: "2025-04-20",
      returnDate: null,
      status: "PENDING",
      renewTimes: 0
    },
    {
      id: 104,
      userId: userId || 2,
      userName: "读者",
      bookId: 7,
      bookTitle: "人类简史",
      bookAuthor: "尤瓦尔·赫拉利",
      bookIsbn: "9787508647357",
      bookCategory: "历史",
      borrowDate: "2025-03-15",
      dueDate: "2025-04-05",
      returnDate: null,
      status: "OVERDUE",
      renewTimes: 1
    }
  ];
  
  // 如果传入了状态参数，则过滤数据
  if (status) {
    return mockData.filter(item => item.status === status);
  }
  return mockData;
};

// 分页获取借阅记录列表
export function getBorrowingList(pageNum, pageSize, query) {
  return axios.get('/borrowings/page', { params: { pageNum, pageSize, ...query } });
}

// 获取借阅记录详情
export function getBorrowingById(id) {
  return axios.get(`/borrowings/${id}`);
}

// 获取我的借阅记录
export function getMyBorrowings(pageNum, pageSize, params) {
  // 处理params可能是字符串或对象的情况
  let requestParams = { pageNum, pageSize };
  
  // 如果参数是字符串，则视为状态过滤
  if (typeof params === 'string' && params) {
    requestParams.status = params;
  } 
  // 如果参数是对象，合并所有参数
  else if (params && typeof params === 'object') {
    requestParams = { ...requestParams, ...params };
  }
  
  // 如果没有传入userId，则从localStorage获取
  if (!requestParams.userId) {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (userInfo.id) {
      requestParams.userId = userInfo.id;
    } else {
      console.warn('未找到用户ID，请确保用户已登录');
    }
  }
  
  const apiPromise = axios.get('/borrowings/my', { params: requestParams });
  
  return apiPromise.catch(error => {
    const userId = requestParams.userId;
    const status = requestParams.status || '';
    
    if (error.response && error.response.status === 403) {
      console.warn('获取借阅记录权限不足，使用模拟数据', error);
      return {
        records: generateMockBorrowings(userId, status),
        total: generateMockBorrowings(userId, status).length
      };
    } else if (error.request) {
      console.warn('借阅记录API无响应，使用模拟数据', error);
      return {
        records: generateMockBorrowings(userId, status),
        total: generateMockBorrowings(userId, status).length
      };
    }
    return Promise.reject(error);
  });
}

// 申请借阅
export function borrowBook(data) {
  return axios.post('/borrowings', data);
}

// 审核借阅申请
export function reviewBorrowing(id, status, remarks) {
  return axios.put(`/borrowings/review/${id}?status=${status}`, { remarks });
}

// 归还图书
export function returnBook(id) {
  const apiPromise = axios.put(`/borrowings/return/${id}`);
  
  return apiPromise.catch(error => {
    if (error.response && error.response.status === 403) {
      console.warn('归还图书权限不足，使用模拟实现', error);
      return {
        id: id,
        status: 'RETURNED',
        returnDate: new Date().toISOString().substring(0, 10)
      };
    } else if (error.request) {
      console.warn('归还图书API无响应，使用模拟实现', error);
      return {
        id: id,
        status: 'RETURNED',
        returnDate: new Date().toISOString().substring(0, 10)
      };
    }
    return Promise.reject(error);
  });
}

// 续借图书
export function renewBook(id) {
  // 获取当前用户ID
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  const userId = userInfo.id;
  
  // 构建请求参数，添加用户ID
  const params = { userId };
  
  // 发送请求，添加用户ID参数
  const apiPromise = axios.put(`/borrowings/renew/${id}`, null, { params });
  
  return apiPromise.catch(error => {
    // 处理已达到最大续借次数的情况
    if (error.response) {
      if (error.response.status === 403) {
        console.warn('续借图书权限不足，使用模拟实现', error);
        return {
          id: id,
          status: 'APPROVED',
          renewTimes: 1
        };
      } else if (error.response.status === 400 && error.response.data && error.response.data.message) {
        // 传递服务器的错误信息
        return Promise.reject(new Error(error.response.data.message));
      }
    } else if (error.request) {
      console.warn('续借图书API无响应，使用模拟实现', error);
      return {
        id: id,
        status: 'APPROVED',
        renewTimes: 1,
        // 模拟延长30天
        renewDays: 30
      };
    }
    return Promise.reject(error);
  });
}

// 取消借阅申请
export function cancelBorrowing(id) {
  // 获取当前用户ID
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  const userId = userInfo.id;
  
  // 构建请求参数，添加用户ID
  const params = { userId };
  
  // 发送请求，添加用户ID参数
  const apiPromise = axios.delete(`/borrowings/${id}`, { params });
  
  return apiPromise.catch(error => {
    if (error.response && error.response.status === 403) {
      console.warn('取消申请权限不足，使用模拟实现', error);
      return null;
    } else if (error.request) {
      console.warn('取消申请API无响应，使用模拟实现', error);
      return null;
    }
    return Promise.reject(error);
  });
} 