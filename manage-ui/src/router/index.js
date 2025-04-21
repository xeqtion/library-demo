import { createRouter, createWebHistory } from 'vue-router';

// 布局组件
const Layout = () => import('../views/layout/index.vue');
// 登录页
const Login = () => import('../views/login/index.vue');

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House' }
      },
      // 用户管理
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      // 图书管理
      {
        path: 'books',
        name: 'Books',
        component: () => import('../views/book/index.vue'),
        meta: { title: '图书管理', icon: 'Reading', roles: ['ADMIN'] }
      },
      // 借阅管理
      {
        path: 'borrowings',
        name: 'Borrowings',
        component: () => import('../views/borrowing/index.vue'),
        meta: { title: '借阅管理', icon: 'Document', roles: ['ADMIN'] }
      },
      // 统计分析
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/statistics/index.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis', roles: ['ADMIN'] }
      },
      // 图书浏览
      {
        path: 'library',
        name: 'Library',
        component: () => import('../views/library/index.vue'),
        meta: { title: '图书浏览', icon: 'Collection' }
      },
      // 我的借阅
      {
        path: 'my-borrowings',
        name: 'MyBorrowings',
        component: () => import('../views/my-borrowing/index.vue'),
        meta: { title: '我的借阅', icon: 'Tickets' }
      },
      // 个人信息
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '个人信息', icon: 'UserFilled' }
      }
    ]
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 图书馆管理系统` : '图书馆管理系统';
  
  // 检查是否需要登录
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 获取用户信息
    const userInfo = localStorage.getItem('userInfo');
    
    if (!userInfo) {
      // 未登录，跳转到登录页
      next({ name: 'Login' });
    } else {
      // 已登录，检查角色权限
      if (to.meta.roles) {
        const user = JSON.parse(userInfo);
        if (to.meta.roles.includes(user.role)) {
          next();
        } else {
          // 无权限，跳转到首页
          next({ name: 'Dashboard' });
        }
      } else {
        next();
      }
    }
  } else {
    next();
  }
});

export default router; 