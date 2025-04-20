package com.manage.librarydemo.config;

import com.manage.librarydemo.entity.Book;
import com.manage.librarydemo.entity.User;
import com.manage.librarydemo.repository.BookRepository;
import com.manage.librarydemo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        initUsers();
        initBooks();
    }

    @Transactional
    public void initUsers() {
        if (userRepository.count() == 0) {
            log.info("初始化用户数据");
            
            // 创建管理员账户
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setName("管理员");
            admin.setEmail("admin@example.com");
            admin.setRole(User.UserRole.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            
            // 创建默认读者账户
            User reader = new User();
            reader.setUsername("reader");
            reader.setPassword(passwordEncoder.encode("123456"));
            reader.setName("读者");
            reader.setEmail("reader@example.com");
            reader.setRole(User.UserRole.READER);
            reader.setEnabled(true);
            userRepository.save(reader);
            
            log.info("用户数据初始化完成");
        }
    }

    @Transactional
    public void initBooks() {
        if (bookRepository.count() == 0) {
            log.info("初始化图书数据");
            
            // 示例图书列表
            List<Book> books = Arrays.asList(
                createBook("Java编程思想", "Bruce Eckel", "计算机科学", "978-7-111-21382-6", 
                       "Java编程思想是Java程序员必读的经典著作", 10, "机械工业出版社", 2007),
                       
                createBook("史记", "司马迁", "历史", "978-7-101-00306-9", 
                       "《史记》是中国历史上第一部纪传体通史", 5, "中华书局", 2016),
                       
                createBook("红楼梦", "曹雪芹", "文学", "978-7-020-02825-2", 
                       "《红楼梦》是中国四大名著之一", 8, "人民文学出版社", 2005),
                
                createBook("算法导论", "Thomas H. Cormen", "计算机科学", "978-7-111-40682-6", 
                       "《算法导论》是一本算法领域的经典教材", 6, "机械工业出版社", 2013),
                
                createBook("1984", "乔治·奥威尔", "文学", "978-7-530-21012-9", 
                       "《1984》是乔治·奥威尔的代表作", 7, "北京十月文艺出版社", 2010),
                
                createBook("三体", "刘慈欣", "科幻", "978-7-535-46796-9", 
                       "《三体》是中国科幻文学的代表作", 15, "重庆出版社", 2008),
                
                createBook("人类简史", "尤瓦尔·赫拉利", "历史", "978-7-508-65022-3", 
                       "《人类简史：从动物到上帝》是以色列历史学家的重要著作", 9, "中信出版社", 2014),
                
                createBook("深入理解计算机系统", "Randal E. Bryant", "计算机科学", "978-7-111-54186-3", 
                       "《深入理解计算机系统》是计算机专业经典教材", 5, "机械工业出版社", 2016),
                
                createBook("明朝那些事儿", "当年明月", "历史", "978-7-213-06268-5", 
                       "《明朝那些事儿》是一部以明朝历史为背景的历史著作", 12, "浙江人民出版社", 2011),
                
                createBook("JavaScript高级程序设计", "Nicholas C. Zakas", "计算机科学", "978-7-115-47929-8", 
                       "《JavaScript高级程序设计》是前端开发工程师必读书籍", 7, "人民邮电出版社", 2019)
            );
            
            bookRepository.saveAll(books);
            
            log.info("图书数据初始化完成");
        }
    }
    
    private Book createBook(String title, String author, String category, String isbn, 
                           String description, Integer totalCopies, String publisher, Integer publishYear) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setIsbn(isbn);
        book.setDescription(description);
        book.setTotalCopies(totalCopies);
        book.setAvailableCopies(totalCopies); // 初始可用数量等于总数量
        book.setPublisher(publisher);
        book.setPublishYear(publishYear);
        return book;
    }
} 