package com.manage.librarydemo.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门图书DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularBookDTO {
    
    /**
     * 图书ID
     */
    private Long id;
    
    /**
     * 书名
     */
    private String title;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * ISBN
     */
    private String isbn;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 总复本数
     */
    private Integer totalCopies;
    
    /**
     * 可借复本数
     */
    private Integer availableCopies;
    
    /**
     * 借阅次数
     */
    private Integer borrowCount;
    
    /**
     * 平均借阅时长（天）
     */
    private Double averageDuration;
} 