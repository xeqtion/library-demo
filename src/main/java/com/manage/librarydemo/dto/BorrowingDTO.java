package com.manage.librarydemo.dto;

import com.manage.librarydemo.entity.Borrowing;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BorrowingDTO {

    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    private String userName;

    private String bookTitle;

    private LocalDate borrowDate;

    private LocalDate dueDate;

    private LocalDate returnDate;

    private Borrowing.BorrowingStatus status;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remarks;
} 