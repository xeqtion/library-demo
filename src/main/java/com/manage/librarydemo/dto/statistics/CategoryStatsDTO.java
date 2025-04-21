package com.manage.librarydemo.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图书分类统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsDTO {
    
    /**
     * 图书分类
     */
    private String category;
    
    /**
     * 借阅次数
     */
    private Integer count;
} 