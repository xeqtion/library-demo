package com.manage.librarydemo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookDTO {

    private Long id;

    @NotBlank(message = "书名不能为空")
    @Size(max = 100, message = "书名长度不能超过100个字符")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(max = 100, message = "作者长度不能超过100个字符")
    private String author;

    @NotBlank(message = "ISBN不能为空")
    @Size(max = 20, message = "ISBN长度不能超过20个字符")
    private String isbn;

    @NotBlank(message = "分类不能为空")
    @Size(max = 50, message = "分类长度不能超过50个字符")
    private String category;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @NotNull(message = "总数量不能为空")
    @Min(value = 1, message = "总数量必须大于0")
    private Integer totalCopies;

    private Integer availableCopies;

    private String coverImage;

    @Size(max = 50, message = "出版社长度不能超过50个字符")
    private String publisher;

    private Integer publishYear;
} 