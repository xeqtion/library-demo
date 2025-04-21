package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.statistics.*;
import com.manage.librarydemo.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计分析控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取借阅趋势数据
     */
    @GetMapping("/borrowing-trend")
    public Result<BorrowingTrendDTO> getBorrowingTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getBorrowingTrend(startDate, endDate));
    }

    /**
     * 获取借阅状态分布数据
     */
    @GetMapping("/borrowing-status")
    public Result<List<BorrowingStatusDTO>> getBorrowingStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getBorrowingStatus(startDate, endDate));
    }

    /**
     * 获取分类统计数据
     */
    @GetMapping("/categories")
    public Result<List<CategoryStatsDTO>> getCategoryStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getCategoryStats(startDate, endDate));
    }

    /**
     * 获取用户借阅排行
     */
    @GetMapping("/user-rank")
    public Result<List<UserRankDTO>> getUserRank(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getUserRank(startDate, endDate));
    }

    /**
     * 获取热门图书排行
     */
    @GetMapping("/popular-books")
    public Result<List<PopularBookDTO>> getPopularBooks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getPopularBooks(startDate, endDate));
    }
} 