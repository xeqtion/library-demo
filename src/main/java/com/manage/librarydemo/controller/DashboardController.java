package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.BookDTO;
import com.manage.librarydemo.dto.BorrowingDTO;
import com.manage.librarydemo.dto.DashboardDTO;
import com.manage.librarydemo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardDTO> getStats() {
        return Result.success(dashboardService.getStats());
    }

    /**
     * 获取最近借阅记录
     */
    @GetMapping("/recent-borrowings")
    public Result<List<BorrowingDTO>> getRecentBorrowings() {
        return Result.success(dashboardService.getRecentBorrowings());
    }

    /**
     * 获取热门图书
     */
    @GetMapping("/popular-books")
    public Result<List<BookDTO>> getPopularBooks() {
        return Result.success(dashboardService.getPopularBooks());
    }
} 