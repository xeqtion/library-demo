package com.manage.librarydemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    
    /**
     * 总图书数
     */
    private Integer totalBooks;
    
    /**
     * 可借图书数
     */
    private Integer availableBooks;
    
    /**
     * 总用户数
     */
    private Integer totalUsers;
    
    /**
     * 今日新增用户
     */
    private Integer newUsers;
    
    /**
     * 总借阅数
     */
    private Integer totalBorrowings;
    
    /**
     * 本月借阅数
     */
    private Integer monthlyBorrowings;
    
    /**
     * 待处理请求数
     */
    private Integer pendingRequests;
    
    /**
     * 逾期未还数
     */
    private Integer overdueBooks;
} 