package com.manage.librarydemo.service;

import com.manage.librarydemo.dto.BookDTO;
import com.manage.librarydemo.dto.BorrowingDTO;
import com.manage.librarydemo.dto.DashboardDTO;

import java.util.List;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     * 
     * @return 仪表盘统计数据
     */
    DashboardDTO getStats();
    
    /**
     * 获取最近借阅记录
     * 
     * @return 最近借阅记录列表
     */
    List<BorrowingDTO> getRecentBorrowings();
    
    /**
     * 获取热门图书
     * 
     * @return 热门图书列表
     */
    List<BookDTO> getPopularBooks();
} 