package com.manage.librarydemo.service.impl;

import com.manage.librarydemo.dto.BookDTO;
import com.manage.librarydemo.dto.BorrowingDTO;
import com.manage.librarydemo.dto.DashboardDTO;
import com.manage.librarydemo.entity.Book;
import com.manage.librarydemo.entity.Borrowing;
import com.manage.librarydemo.entity.User;
import com.manage.librarydemo.repository.BookRepository;
import com.manage.librarydemo.repository.BorrowingRepository;
import com.manage.librarydemo.repository.UserRepository;
import com.manage.librarydemo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;

    /**
     * 获取仪表盘统计数据
     *
     * @return 仪表盘统计数据
     */
    @Override
    public DashboardDTO getStats() {
        // 获取图书相关统计
        long totalBooks = bookRepository.count();
        long availableBooks = bookRepository.findAll().stream()
                .mapToInt(Book::getAvailableCopies)
                .sum();

        // 获取用户相关统计
        long totalUsers = userRepository.count();
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long newUsers = userRepository.findAll().stream()
                .filter(user -> user.getCreateTime().isAfter(today))
                .count();

        // 获取借阅相关统计
        List<Borrowing> allBorrowings = borrowingRepository.findAll();
        long totalBorrowings = allBorrowings.size();

        // 本月借阅数
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        long monthlyBorrowings = allBorrowings.stream()
                .filter(b -> b.getBorrowDate().isAfter(firstDayOfMonth) || b.getBorrowDate().isEqual(firstDayOfMonth))
                .count();

        // 待处理请求数
        long pendingRequests = allBorrowings.stream()
                .filter(b -> b.getStatus() == Borrowing.BorrowingStatus.PENDING)
                .count();

        // 逾期未还数
        LocalDate today2 = LocalDate.now();
        long overdueBooks = allBorrowings.stream()
                .filter(b -> (b.getStatus() == Borrowing.BorrowingStatus.APPROVED || b.getStatus() == Borrowing.BorrowingStatus.OVERDUE)
                        && b.getReturnDate() == null && b.getDueDate().isBefore(today2))
                .count();

        // 构建返回数据
        return DashboardDTO.builder()
                .totalBooks((int) totalBooks)
                .availableBooks((int) availableBooks)
                .totalUsers((int) totalUsers)
                .newUsers((int) newUsers)
                .totalBorrowings((int) totalBorrowings)
                .monthlyBorrowings((int) monthlyBorrowings)
                .pendingRequests((int) pendingRequests)
                .overdueBooks((int) overdueBooks)
                .build();
    }

    /**
     * 获取最近借阅记录
     *
     * @return 最近借阅记录列表
     */
    @Override
    public List<BorrowingDTO> getRecentBorrowings() {
        // 获取最近10条借阅记录
        List<Borrowing> recentBorrowings = borrowingRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createTime"))
        ).getContent();

        // 转换为DTO
        return recentBorrowings.stream()
                .map(this::convertToBorrowingDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门图书
     *
     * @return 热门图书列表
     */
    @Override
    public List<BookDTO> getPopularBooks() {
        // 获取所有借阅记录
        List<Borrowing> allBorrowings = borrowingRepository.findAll();

        // 按图书分组统计借阅次数
        Map<Book, Long> bookBorrowCountMap = allBorrowings.stream()
                .collect(Collectors.groupingBy(Borrowing::getBook, Collectors.counting()));

        // 按借阅次数排序，取前10
        List<Book> popularBooks = bookBorrowCountMap.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 转换为DTO
        List<BookDTO> result = new ArrayList<>();
        for (Book book : popularBooks) {
            BookDTO dto = new BookDTO();
            BeanUtils.copyProperties(book, dto);
            
            // 添加借阅次数
            dto.setBorrowCount(bookBorrowCountMap.get(book).intValue());
            
            result.add(dto);
        }

        return result;
    }

    /**
     * 将借阅实体转换为DTO
     */
    private BorrowingDTO convertToBorrowingDTO(Borrowing borrowing) {
        BorrowingDTO dto = new BorrowingDTO();
        dto.setId(borrowing.getId());
        dto.setUserId(borrowing.getUser().getId());
        dto.setUserName(borrowing.getUser().getName() != null && !borrowing.getUser().getName().isEmpty() ? 
                borrowing.getUser().getName() : borrowing.getUser().getUsername());
        dto.setBookId(borrowing.getBook().getId());
        dto.setBookTitle(borrowing.getBook().getTitle());
        dto.setBookAuthor(borrowing.getBook().getAuthor());
        dto.setBookIsbn(borrowing.getBook().getIsbn());
        dto.setBookCategory(borrowing.getBook().getCategory());
        dto.setBookCover(borrowing.getBook().getCoverImage());
        dto.setBorrowDate(borrowing.getBorrowDate());
        dto.setDueDate(borrowing.getDueDate());
        dto.setReturnDate(borrowing.getReturnDate());
        dto.setStatus(borrowing.getStatus());
        dto.setRemarks(borrowing.getRemarks());
        return dto;
    }
} 