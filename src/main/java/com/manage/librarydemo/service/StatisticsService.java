package com.manage.librarydemo.service;

import com.manage.librarydemo.dto.statistics.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {

    /**
     * 获取借阅趋势数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 借阅趋势数据
     */
    BorrowingTrendDTO getBorrowingTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取借阅状态分布
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 借阅状态分布数据
     */
    List<BorrowingStatusDTO> getBorrowingStatus(LocalDate startDate, LocalDate endDate);

    /**
     * 获取分类统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分类统计数据
     */
    List<CategoryStatsDTO> getCategoryStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取用户借阅排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 用户借阅排行数据
     */
    List<UserRankDTO> getUserRank(LocalDate startDate, LocalDate endDate);

    /**
     * 获取热门图书排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 热门图书排行数据
     */
    List<PopularBookDTO> getPopularBooks(LocalDate startDate, LocalDate endDate);
} 