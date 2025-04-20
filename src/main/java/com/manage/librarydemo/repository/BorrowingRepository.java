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
} 