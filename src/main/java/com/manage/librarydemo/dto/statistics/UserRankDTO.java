package com.manage.librarydemo.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户借阅排行DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRankDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 借阅次数
     */
    private Integer count;
} 