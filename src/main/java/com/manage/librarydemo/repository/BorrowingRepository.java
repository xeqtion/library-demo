package com.manage.librarydemo.repository;

import com.manage.librarydemo.entity.Borrowing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    Page<Borrowing> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT b FROM Borrowing b WHERE b.status = 'BORROWED' AND b.dueDate < :today")
    List<Borrowing> findOverdueBooks(LocalDate today);

    @Query("SELECT b FROM Borrowing b JOIN b.user u JOIN b.book bk WHERE " +
            "(u.username LIKE %:keyword% OR u.name LIKE %:keyword% OR bk.title LIKE %:keyword%) " +
            "AND (:status IS NULL OR b.status = :status)")
    Page<Borrowing> findByKeywordAndStatus(String keyword, Borrowing.BorrowingStatus status, Pageable pageable);

    long countByBookIdAndStatusIn(Long bookId, List<Borrowing.BorrowingStatus> statusList);
    
    long countByUserIdAndStatusIn(Long userId, List<Borrowing.BorrowingStatus> statusList);
    
    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, List<Borrowing.BorrowingStatus> statusList);
    
    // 统计分析相关查询方法
    
    /**
     * 查询指定日期范围内的借阅记录
     */
    @Query("SELECT b FROM Borrowing b WHERE b.borrowDate BETWEEN :startDate AND :endDate")
    List<Borrowing> findByBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 查询指定日期范围内的归还记录
     */
    @Query("SELECT b FROM Borrowing b WHERE b.returnDate BETWEEN :startDate AND :endDate")
    List<Borrowing> findByReturnDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 按状态统计借阅数量
     */
    @Query("SELECT b.status as status, COUNT(b) as count FROM Borrowing b WHERE b.borrowDate BETWEEN :startDate AND :endDate GROUP BY b.status")
    List<Object[]> countByStatusAndBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 按分类统计借阅数量
     */
    @Query("SELECT b.book.category as category, COUNT(b) as count FROM Borrowing b WHERE b.borrowDate BETWEEN :startDate AND :endDate GROUP BY b.book.category")
    List<Object[]> countByCategoryAndBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 按用户统计借阅数量
     */
    @Query("SELECT b.user.id as userId, b.user.name as userName, b.user.username as username, COUNT(b) as count FROM Borrowing b WHERE b.borrowDate BETWEEN :startDate AND :endDate GROUP BY b.user.id, b.user.name, b.user.username ORDER BY COUNT(b) DESC")
    List<Object[]> countByUserAndBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 按图书统计借阅数量
     */
    @Query("SELECT b.book.id as bookId, COUNT(b) as count FROM Borrowing b WHERE b.borrowDate BETWEEN :startDate AND :endDate GROUP BY b.book.id ORDER BY COUNT(b) DESC")
    List<Object[]> countByBookAndBorrowDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取已经归还的借阅记录，用于计算平均借阅时长
     */
    @Query("SELECT b FROM Borrowing b WHERE b.borrowDate BETWEEN :startDate AND :endDate AND b.returnDate IS NOT NULL")
    List<Borrowing> findReturnedBorrowingsByBorrowDateBetween(LocalDate startDate, LocalDate endDate);
} 