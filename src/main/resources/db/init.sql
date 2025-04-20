-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建图书表
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    cover_image VARCHAR(200),
    publisher VARCHAR(50),
    publish_year INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建借阅表
CREATE TABLE IF NOT EXISTS borrowings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remarks VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);


-- 初始化图书数据
INSERT INTO books (title, author, isbn, category, description, total_copies, available_copies, publisher, publish_year) VALUES
('Java编程思想', 'Bruce Eckel', '9787111213826', '计算机科学', 'Java领域的经典著作', 10, 8, '机械工业出版社', 2007),
('深入理解Java虚拟机', '周志明', '9787111641247', '计算机科学', '深入讲解JVM的原理与实践', 5, 4, '机械工业出版社', 2019),
('数据结构与算法分析', 'Mark Allen Weiss', '9787302330646', '计算机科学', '算法与数据结构的经典教材', 6, 6, '清华大学出版社', 2014),
('三体', '刘慈欣', '9787536692930', '科幻小说', '中国科幻文学的代表作', 15, 12, '重庆出版社', 2008),
('活着', '余华', '9787506365437', '文学小说', '讲述一个人历经世间沧桑的人生故事', 8, 7, '作家出版社', 2012);

-- 初始化借阅数据
INSERT INTO borrowings (user_id, book_id, borrow_date, due_date, status) VALUES
(2, 1, '2023-09-01', '2023-09-15', 'BORROWED'),
(2, 4, '2023-09-05', '2023-09-19', 'BORROWED'),
(3, 2, '2023-09-03', '2023-09-17', 'BORROWED'); 