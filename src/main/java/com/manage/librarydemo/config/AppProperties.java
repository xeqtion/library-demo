package com.manage.librarydemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Borrowing borrowing = new Borrowing();
    private final Admin admin = new Admin();

    @Data
    public static class Jwt {
        private String secret = "librarymanagementsystemsecretkeyforauthentication";
        private long expiration = 86400000; // 24小时
    }
    
    @Data
    public static class Borrowing {
        private int maxBooksPerUser = 5; // 每个用户最多可借阅的图书数量
        private int defaultBorrowDays = 14; // 默认借阅天数
        private int maxRenewTimes = 1; // 最大续借次数，修改为1次
        private int renewDays = 30; // 每次续借增加的天数，修改为30天
    }
    
    @Data
    public static class Admin {
        private String registerCode = "ADMIN_SECRET_2023"; // 管理员注册码，建议在配置文件中修改
    }
} 