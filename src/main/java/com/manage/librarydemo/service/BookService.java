package com.manage.librarydemo.service;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.dto.BookDTO;
import com.manage.librarydemo.entity.Book;

import java.util.List;

public interface BookService {

    PageResult<BookDTO> getPage(PageRequest pageRequest);
    
    BookDTO getById(Long id);
    
    BookDTO add(BookDTO bookDTO);
    
    BookDTO update(BookDTO bookDTO);
    
    void delete(Long id);
    
    Book findBookEntity(Long id);
    
    List<String> getAllCategories();
    
    PageResult<BookDTO> getByCategory(String category, PageRequest pageRequest);
} 