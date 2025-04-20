package com.manage.librarydemo.validator;

import com.manage.librarydemo.config.AppProperties;
import com.manage.librarydemo.entity.Borrowing;
import com.manage.librarydemo.repository.BorrowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BorrowingValidator {

    private final BorrowingRepository borrowingRepository;
    private final AppProperties appProperties;

    /**
     * 检查用户是否可以借阅图书
     * @param userId 用户ID
     * @return 是否可以借阅
     */
    public boolean canBorrow(Long userId) {
        // 查询用户当前借阅数量（包括已借出和逾期的）
        long count = borrowingRepository.countByUserIdAndStatusIn(
                userId, 
                Arrays.asList(Borrowing.BorrowingStatus.BORROWED, Borrowing.BorrowingStatus.OVERDUE)
        );
        
        // 检查是否超过最大借阅数量
        return count < appProperties.getBorrowing().getMaxBooksPerUser();
    }
    
    /**
     * 检查用户是否已经借阅或申请借阅此图书
     * @param userId 用户ID
     * @param bookId 图书ID
     * @return 是否已经借阅
     */
    public boolean hasBorrowed(Long userId, Long bookId) {
        // 查询用户是否已有该图书的借阅记录（除了已归还状态）
        List<Borrowing.BorrowingStatus> statuses = Arrays.asList(
                Borrowing.BorrowingStatus.PENDING,
                Borrowing.BorrowingStatus.BORROWED,
                Borrowing.BorrowingStatus.OVERDUE
        );
        
        return borrowingRepository.existsByUserIdAndBookIdAndStatusIn(userId, bookId, statuses);
    }
    
    /**
     * 获取用户当前借阅数量
     * @param userId 用户ID
     * @return 当前借阅数量
     */
    public int getCurrentBorrowCount(Long userId) {
        return (int) borrowingRepository.countByUserIdAndStatusIn(
                userId, 
                Arrays.asList(Borrowing.BorrowingStatus.BORROWED, Borrowing.BorrowingStatus.OVERDUE)
        );
    }
} 