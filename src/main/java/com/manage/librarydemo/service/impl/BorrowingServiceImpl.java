package com.manage.librarydemo.service.impl;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.config.AppProperties;
import com.manage.librarydemo.dto.BorrowingDTO;
import com.manage.librarydemo.entity.Book;
import com.manage.librarydemo.entity.Borrowing;
import com.manage.librarydemo.entity.User;
import com.manage.librarydemo.repository.BorrowingRepository;
import com.manage.librarydemo.service.BookService;
import com.manage.librarydemo.service.BorrowingService;
import com.manage.librarydemo.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserService userService;
    private final BookService bookService;
    private final AppProperties appProperties;

    @Override
    public PageResult<BorrowingDTO> getPage(PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPageNum() - 1,
                pageRequest.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Page<Borrowing> page;
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            page = borrowingRepository.findByKeywordAndStatus(pageRequest.getKeyword(), null, pageable);
        } else {
            page = borrowingRepository.findAll(pageable);
        }

        return PageResult.of(
                page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                page.getTotalElements(),
                pageRequest.getPageNum(),
                pageRequest.getPageSize()
        );
    }

    @Override
    public PageResult<BorrowingDTO> getByUser(Long userId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPageNum() - 1,
                pageRequest.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Page<Borrowing> page = borrowingRepository.findByUserId(userId, pageable);

        return PageResult.of(
                page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                page.getTotalElements(),
                pageRequest.getPageNum(),
                pageRequest.getPageSize()
        );
    }

    @Override
    public BorrowingDTO getById(Long id) {
        return borrowingRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
    }

    @Override
    @Transactional
    public BorrowingDTO add(BorrowingDTO borrowingDTO) {
        // 验证用户是否存在
        User user = userService.findUserEntity(borrowingDTO.getUserId());
        
        // 验证图书是否存在
        Book book = bookService.findBookEntity(borrowingDTO.getBookId());
        
        // 检查图书是否有可用库存
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("图书库存不足");
        }
        
        // 检查用户借阅数量是否超过限制
        long userBorrowedCount = borrowingRepository.countByBookIdAndStatusIn(
                book.getId(), 
                Arrays.asList(Borrowing.BorrowingStatus.BORROWED, Borrowing.BorrowingStatus.OVERDUE)
        );
        if (userBorrowedCount >= appProperties.getBorrowing().getMaxBooksPerUser()) {
            throw new IllegalArgumentException("您已达到最大借阅数量限制(" + 
                    appProperties.getBorrowing().getMaxBooksPerUser() + "本)");
        }
        
        // 创建借阅记录
        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setStatus(Borrowing.BorrowingStatus.PENDING);
        
        // 设置默认借阅日期和归还日期
        LocalDate now = LocalDate.now();
        borrowing.setBorrowDate(now);
        borrowing.setDueDate(now.plusDays(appProperties.getBorrowing().getDefaultBorrowDays()));
        
        if (StringUtils.hasText(borrowingDTO.getRemarks())) {
            borrowing.setRemarks(borrowingDTO.getRemarks());
        }
        
        borrowing = borrowingRepository.save(borrowing);
        
        return convertToDTO(borrowing);
    }

    @Override
    @Transactional
    public BorrowingDTO review(Long id, Borrowing.BorrowingStatus status, String remarks) {
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        
        // 只有待审核状态才能审核
        if (borrowing.getStatus() != Borrowing.BorrowingStatus.PENDING) {
            throw new IllegalArgumentException("只能审核待处理的借阅申请");
        }
        
        // 更新状态
        borrowing.setStatus(status);
        
        // 添加备注
        if (StringUtils.hasText(remarks)) {
            borrowing.setRemarks(remarks);
        }
        
        // 如果审核通过，更新图书库存
        if (status == Borrowing.BorrowingStatus.BORROWED) {
            Book book = borrowing.getBook();
            if (book.getAvailableCopies() <= 0) {
                throw new IllegalArgumentException("图书库存不足");
            }
            book.setAvailableCopies(book.getAvailableCopies() - 1);
        }
        
        borrowing = borrowingRepository.save(borrowing);
        
        return convertToDTO(borrowing);
    }

    @Override
    @Transactional
    public BorrowingDTO returnBook(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        
        // 只有借出状态才能归还
        if (borrowing.getStatus() != Borrowing.BorrowingStatus.BORROWED && 
                borrowing.getStatus() != Borrowing.BorrowingStatus.OVERDUE) {
            throw new IllegalArgumentException("只能归还已借出或逾期的图书");
        }
        
        // 更新状态
        borrowing.setStatus(Borrowing.BorrowingStatus.RETURNED);
        
        // 设置归还日期
        borrowing.setReturnDate(LocalDate.now());
        
        // 更新图书库存
        Book book = borrowing.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        
        borrowing = borrowingRepository.save(borrowing);
        
        return convertToDTO(borrowing);
    }

    @Override
    @Transactional
    public BorrowingDTO renewBook(Long id, Long userId) {
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        
        // 验证用户权限
        if (userId != null && !borrowing.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("没有权限操作此借阅记录");
        }
        
        // 只有借出状态才能续借
        if (borrowing.getStatus() != Borrowing.BorrowingStatus.BORROWED) {
            throw new IllegalArgumentException("只能续借已借出的图书");
        }
        
        // 检查是否已达到最大续借次数
        int renewTimes = 0;
        String remarks = borrowing.getRemarks();
        if (remarks != null && remarks.contains("续借次数:")) {
            String[] parts = remarks.split("续借次数:");
            if (parts.length > 1) {
                try {
                    renewTimes = Integer.parseInt(parts[1].trim().split(" ")[0]);
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }
        
        // 检查是否已达到最大续借次数(最多1次)
        int maxRenewTimes = appProperties.getBorrowing().getMaxRenewTimes();
        if (renewTimes >= maxRenewTimes) {
            throw new IllegalArgumentException("已达到最大续借次数(" + 
                    maxRenewTimes + "次)");
        }
        
        // 延长归还日期（30天）
        int renewDays = appProperties.getBorrowing().getRenewDays();
        borrowing.setDueDate(borrowing.getDueDate().plusDays(renewDays));
        
        // 更新续借次数
        renewTimes++;
        String renewInfo = "续借次数: " + renewTimes + " (+" + renewDays + "天, " + 
                LocalDate.now().toString() + ")";
        
        if (remarks == null || remarks.isEmpty()) {
            borrowing.setRemarks(renewInfo);
        } else if (remarks.contains("续借次数:")) {
            borrowing.setRemarks(remarks.replaceAll("续借次数:.*?[\\)\\n]", renewInfo + ")"));
        } else {
            borrowing.setRemarks(remarks + " " + renewInfo);
        }
        
        borrowing = borrowingRepository.save(borrowing);
        
        return convertToDTO(borrowing);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        
        // 验证用户权限
        if (userId != null && !borrowing.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("没有权限操作此借阅记录");
        }
        
        // 只有待审核状态才能取消
        if (borrowing.getStatus() != Borrowing.BorrowingStatus.PENDING) {
            throw new IllegalArgumentException("只能取消待审核的借阅申请");
        }
        
        borrowingRepository.deleteById(id);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    @Transactional
    public void checkOverdue() {
        LocalDate today = LocalDate.now();
        List<Borrowing> overdueList = borrowingRepository.findOverdueBooks(today);
        
        for (Borrowing borrowing : overdueList) {
            borrowing.setStatus(Borrowing.BorrowingStatus.OVERDUE);
            borrowingRepository.save(borrowing);
        }
    }

    @Override
    @Transactional
    public BorrowingDTO renewBook(Long id) {
        // 调用带用户ID的方法，传入null表示管理员操作
        return renewBook(id, null);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        // 调用带用户ID的方法，传入null表示管理员操作
        cancel(id, null);
    }

    private BorrowingDTO convertToDTO(Borrowing borrowing) {
        BorrowingDTO dto = new BorrowingDTO();
        BeanUtils.copyProperties(borrowing, dto);
        
        // 设置关联对象的ID
        dto.setUserId(borrowing.getUser().getId());
        dto.setBookId(borrowing.getBook().getId());
        
        // 设置关联对象的名称
        dto.setUserName(borrowing.getUser().getUsername());
        dto.setBookTitle(borrowing.getBook().getTitle());
        
        return dto;
    }
} 