package com.manage.librarydemo.service;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.dto.BorrowingDTO;
import com.manage.librarydemo.entity.Borrowing;

public interface BorrowingService {

    PageResult<BorrowingDTO> getPage(PageRequest pageRequest);
    
    PageResult<BorrowingDTO> getByUser(Long userId, PageRequest pageRequest);
    
    BorrowingDTO getById(Long id);
    
    BorrowingDTO add(BorrowingDTO borrowingDTO);
    
    BorrowingDTO review(Long id, Borrowing.BorrowingStatus status, String remarks);
    
    BorrowingDTO returnBook(Long id);
    
    BorrowingDTO renewBook(Long id);
    
    BorrowingDTO renewBook(Long id, Long userId);
    
    void cancel(Long id);
    
    void cancel(Long id, Long userId);
    
    void checkOverdue();
} 