package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.BorrowingDTO;
import com.manage.librarydemo.entity.Borrowing;
import com.manage.librarydemo.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping("/page")
    public Result<PageResult<BorrowingDTO>> getPage(PageRequest pageRequest) {
        return Result.success(borrowingService.getPage(pageRequest));
    }

    @GetMapping("/{id}")
    public Result<BorrowingDTO> getById(@PathVariable Long id) {
        return Result.success(borrowingService.getById(id));
    }

    @GetMapping("/my")
    public Result<PageResult<BorrowingDTO>> getMyBorrowings(PageRequest pageRequest, @RequestParam Long userId) {
        return Result.success(borrowingService.getByUser(userId, pageRequest));
    }

    @PostMapping
    public Result<BorrowingDTO> add(@Valid @RequestBody BorrowingDTO borrowingDTO) {
        return Result.success(borrowingService.add(borrowingDTO));
    }

    @PutMapping("/review/{id}")
    public Result<BorrowingDTO> review(@PathVariable Long id, 
                                      @RequestParam Borrowing.BorrowingStatus status,
                                      @RequestParam(required = false) String remarks) {
        return Result.success(borrowingService.review(id, status, remarks));
    }

    @PutMapping("/return/{id}")
    public Result<BorrowingDTO> returnBook(@PathVariable Long id) {
        return Result.success(borrowingService.returnBook(id));
    }

    @PutMapping("/renew/{id}")
    public Result<BorrowingDTO> renewBook(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return Result.success(borrowingService.renewBook(id, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        borrowingService.cancel(id, userId);
        return Result.success();
    }
} 