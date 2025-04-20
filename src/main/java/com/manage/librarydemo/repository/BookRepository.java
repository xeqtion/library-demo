package com.manage.librarydemo.repository;

import com.manage.librarydemo.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword% OR b.category LIKE %:keyword%")
    Page<Book> findByKeyword(String keyword, Pageable pageable);

    Page<Book> findByCategory(String category, Pageable pageable);
} 