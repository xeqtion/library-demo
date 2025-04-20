package com.manage.librarydemo.service.impl;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.dto.BookDTO;
import com.manage.librarydemo.entity.Book;
import com.manage.librarydemo.repository.BookRepository;
import com.manage.librarydemo.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public PageResult<BookDTO> getPage(PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPageNum() - 1,
                pageRequest.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Page<Book> page;
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            page = bookRepository.findByKeyword(pageRequest.getKeyword(), pageable);
        } else {
            page = bookRepository.findAll(pageable);
        }

        return PageResult.of(
                page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                page.getTotalElements(),
                pageRequest.getPageNum(),
                pageRequest.getPageSize()
        );
    }

    @Override
    public BookDTO getById(Long id) {
        return bookRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
    }

    @Override
    @Transactional
    public BookDTO add(BookDTO bookDTO) {
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new IllegalArgumentException("ISBN已存在");
        }

        Book book = new Book();
        BeanUtils.copyProperties(bookDTO, book);
        
        // 可用数量默认等于总数量
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }

        book = bookRepository.save(book);
        return convertToDTO(book);
    }

    @Override
    @Transactional
    public BookDTO update(BookDTO bookDTO) {
        Book book = findBookEntity(bookDTO.getId());
        
        // 如果修改了ISBN，检查是否重复
        if (!book.getIsbn().equals(bookDTO.getIsbn()) && 
                bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new IllegalArgumentException("ISBN已存在");
        }
        
        BeanUtils.copyProperties(bookDTO, book);
        book = bookRepository.save(book);
        return convertToDTO(book);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Book findBookEntity(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
    }
    
    @Override
    public List<String> getAllCategories() {
        return bookRepository.findAll().stream()
                .map(Book::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<BookDTO> getByCategory(String category, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPageNum() - 1,
                pageRequest.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );
        
        Page<Book> page = bookRepository.findByCategory(category, pageable);
        
        return PageResult.of(
                page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                page.getTotalElements(),
                pageRequest.getPageNum(),
                pageRequest.getPageSize()
        );
    }

    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        BeanUtils.copyProperties(book, dto);
        return dto;
    }
} 