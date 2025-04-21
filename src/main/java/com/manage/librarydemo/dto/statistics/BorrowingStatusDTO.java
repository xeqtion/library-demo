package com.manage.librarydemo.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 借阅状态统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingStatusDTO {
    
    /**
     * 借阅状态
     */
    private String status;
    
    /**
     * 数量
     */
    private Integer count;
} 