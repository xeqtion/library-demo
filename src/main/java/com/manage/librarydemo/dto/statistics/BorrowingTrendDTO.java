package com.manage.librarydemo.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 借阅趋势DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingTrendDTO {
    
    /**
     * 日期列表
     */
    private List<String> dates;
    
    /**
     * 借阅数量列表
     */
    private List<Integer> borrowings;
    
    /**
     * 归还数量列表
     */
    private List<Integer> returns;
} 