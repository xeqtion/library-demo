package com.manage.librarydemo.service.impl;

import com.manage.librarydemo.dto.statistics.*;
import com.manage.librarydemo.entity.Book;
import com.manage.librarydemo.entity.Borrowing;
import com.manage.librarydemo.entity.User;
import com.manage.librarydemo.repository.BookRepository;
import com.manage.librarydemo.repository.BorrowingRepository;
import com.manage.librarydemo.repository.UserRepository;
import com.manage.librarydemo.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务实现
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * 获取借阅趋势数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 借阅趋势数据
     */
    @Override
    public BorrowingTrendDTO getBorrowingTrend(LocalDate startDate, LocalDate endDate) {
        // 使用优化的查询方法获取日期范围内的所有借阅记录
        List<Borrowing> borrowings = borrowingRepository.findByBorrowDateBetween(startDate, endDate);
        
        // 使用优化的查询方法获取日期范围内的所有归还记录
        List<Borrowing> returns = borrowingRepository.findByReturnDateBetween(startDate, endDate);

        // 统计每天的借阅和归还数量
        Map<LocalDate, Integer> borrowingCountMap = new HashMap<>();
        Map<LocalDate, Integer> returnCountMap = new HashMap<>();

        // 初始化日期范围内的所有日期
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            borrowingCountMap.put(date, 0);
            returnCountMap.put(date, 0);
            date = date.plusDays(1);
        }

        // 统计借阅数量
        for (Borrowing borrowing : borrowings) {
            LocalDate borrowDate = borrowing.getBorrowDate();
            borrowingCountMap.put(borrowDate, borrowingCountMap.getOrDefault(borrowDate, 0) + 1);
        }

        // 统计归还数量
        for (Borrowing borrowing : returns) {
            LocalDate returnDate = borrowing.getReturnDate();
            if (returnDate != null) {
                returnCountMap.put(returnDate, returnCountMap.getOrDefault(returnDate, 0) + 1);
            }
        }

        // 按日期排序
        List<LocalDate> sortedDates = new ArrayList<>(borrowingCountMap.keySet());
        Collections.sort(sortedDates);

        // 格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<String> dateStrings = sortedDates.stream()
                .map(d -> d.format(formatter))
                .collect(Collectors.toList());

        // 提取借阅和归还数量
        List<Integer> borrowingCounts = sortedDates.stream()
                .map(borrowingCountMap::get)
                .collect(Collectors.toList());

        List<Integer> returnCounts = sortedDates.stream()
                .map(returnCountMap::get)
                .collect(Collectors.toList());

        // 构建返回数据
        return BorrowingTrendDTO.builder()
                .dates(dateStrings)
                .borrowings(borrowingCounts)
                .returns(returnCounts)
                .build();
    }

    /**
     * 获取借阅状态分布
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 借阅状态分布数据
     */
    @Override
    public List<BorrowingStatusDTO> getBorrowingStatus(LocalDate startDate, LocalDate endDate) {
        // 使用优化的查询方法按状态统计借阅数量
        List<Object[]> statusCounts = borrowingRepository.countByStatusAndBorrowDateBetween(startDate, endDate);
        
        // 构建返回数据
        return statusCounts.stream()
                .map(row -> BorrowingStatusDTO.builder()
                        .status(((Borrowing.BorrowingStatus) row[0]).name())
                        .count(((Number) row[1]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 获取分类统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分类统计数据
     */
    @Override
    public List<CategoryStatsDTO> getCategoryStats(LocalDate startDate, LocalDate endDate) {
        // 使用优化的查询方法按分类统计借阅数量
        List<Object[]> categoryCounts = borrowingRepository.countByCategoryAndBorrowDateBetween(startDate, endDate);
        
        // 构建返回数据
        return categoryCounts.stream()
                .map(row -> CategoryStatsDTO.builder()
                        .category((String) row[0])
                        .count(((Number) row[1]).intValue())
                        .build())
                .sorted(Comparator.comparing(CategoryStatsDTO::getCount).reversed()) // 按借阅次数降序排序
                .limit(10) // 只返回前10个
                .collect(Collectors.toList());
    }

    /**
     * 获取用户借阅排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 用户借阅排行数据
     */
    @Override
    public List<UserRankDTO> getUserRank(LocalDate startDate, LocalDate endDate) {
        // 使用优化的查询方法按用户统计借阅数量
        List<Object[]> userCounts = borrowingRepository.countByUserAndBorrowDateBetween(startDate, endDate);
        
        // 构建返回数据
        return userCounts.stream()
                .map(row -> {
                    Long userId = ((Number) row[0]).longValue();
                    String userName = (String) row[1];
                    String username = (String) row[2];
                    Integer count = ((Number) row[3]).intValue();
                    
                    // 如果name为空，使用username
                    String displayName = (userName != null && !userName.isEmpty()) ? userName : username;
                    
                    return UserRankDTO.builder()
                            .userId(userId)
                            .userName(displayName)
                            .count(count)
                            .build();
                })
                .limit(10) // 只返回前10个
                .collect(Collectors.toList());
    }

    /**
     * 获取热门图书排行
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 热门图书排行数据
     */
    @Override
    public List<PopularBookDTO> getPopularBooks(LocalDate startDate, LocalDate endDate) {
        // 使用优化的查询方法按图书统计借阅数量
        List<Object[]> bookCounts = borrowingRepository.countByBookAndBorrowDateBetween(startDate, endDate);
        
        // 获取已归还的借阅记录
        List<Borrowing> returnedBorrowings = borrowingRepository.findReturnedBorrowingsByBorrowDateBetween(startDate, endDate);
        
        // 按图书ID分组，计算平均借阅时长
        Map<Long, List<Borrowing>> bookBorrowingsMap = returnedBorrowings.stream()
                .collect(Collectors.groupingBy(b -> b.getBook().getId()));
                
        Map<Long, Double> bookDurationMap = new HashMap<>();
        
        // 计算每本书的平均借阅时长
        for (Map.Entry<Long, List<Borrowing>> entry : bookBorrowingsMap.entrySet()) {
            Long bookId = entry.getKey();
            List<Borrowing> bookBorrowings = entry.getValue();
            
            double totalDuration = 0;
            for (Borrowing borrowing : bookBorrowings) {
                LocalDate borrowDate = borrowing.getBorrowDate();
                LocalDate returnDate = borrowing.getReturnDate();
                
                if (returnDate != null) {
                    // 计算借阅天数
                    long days = Duration.between(borrowDate.atStartOfDay(), returnDate.atStartOfDay()).toDays();
                    totalDuration += days;
                }
            }
            
            // 计算平均借阅时长
            double avgDuration = bookBorrowings.size() > 0 ? totalDuration / bookBorrowings.size() : 0;
            bookDurationMap.put(bookId, avgDuration);
        }
        
        // 构建热门图书列表
        List<PopularBookDTO> result = new ArrayList<>();
        
        for (Object[] row : bookCounts) {
            Long bookId = ((Number) row[0]).longValue();
            Integer count = ((Number) row[1]).intValue();
            
            // 通过ID查询图书详情
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                
                // 获取平均借阅时长，如果没有则默认为0
                double avgDuration = bookDurationMap.getOrDefault(bookId, 0.0);
                
                // 构建DTO
                PopularBookDTO dto = PopularBookDTO.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .isbn(book.getIsbn())
                        .category(book.getCategory())
                        .coverImage(book.getCoverImage())
                        .totalCopies(book.getTotalCopies())
                        .availableCopies(book.getAvailableCopies())
                        .borrowCount(count)
                        .averageDuration(Math.round(avgDuration * 10) / 10.0) // 四舍五入到一位小数
                        .build();
                
                result.add(dto);
            }
        }
        
        // 按借阅次数降序排序
        return result.stream()
                .sorted(Comparator.comparing(PopularBookDTO::getBorrowCount).reversed())
                .limit(20) // 只返回前20个
                .collect(Collectors.toList());
    }
} 