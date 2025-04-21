package com.manage.librarydemo.dto;

import com.manage.librarydemo.entity.Borrowing;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingDTO {
    
    private Long id;
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    private String userName;
    
    @NotNull(message = "图书ID不能为空")
    private Long bookId;
    
    private String bookTitle;
    
    private String bookAuthor;
    
    private String bookIsbn;
    
    private String bookCategory;
    
    private String bookCover;
    
    private LocalDate borrowDate;
    
    private LocalDate dueDate;
    
    private LocalDate returnDate;
    
    private Borrowing.BorrowingStatus status;
    
    private String remarks;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
} 