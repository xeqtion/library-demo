package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.BookDTO;
import com.manage.librarydemo.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/page")
    public Result<PageResult<BookDTO>> getPage(PageRequest pageRequest) {
        return Result.success(bookService.getPage(pageRequest));
    }

    @GetMapping("/{id}")
    public Result<BookDTO> getById(@PathVariable Long id) {
        return Result.success(bookService.getById(id));
    }

    @PostMapping
    public Result<BookDTO> add(@Valid @RequestBody BookDTO bookDTO) {
        return Result.success(bookService.add(bookDTO));
    }

    @PutMapping
    public Result<BookDTO> update(@Valid @RequestBody BookDTO bookDTO) {
        return Result.success(bookService.update(bookDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return Result.success();
    }
    
    @GetMapping("/categories")
    public Result<List<String>> getAllCategories() {
        return Result.success(bookService.getAllCategories());
    }
    
    @GetMapping("/category/{category}")
    public Result<PageResult<BookDTO>> getByCategory(@PathVariable String category, PageRequest pageRequest) {
        return Result.success(bookService.getByCategory(category, pageRequest));
    }
} 