package com.manage.librarydemo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String name;

    @Column(length = 11)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.READER;

    @Column(nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    public enum UserRole {
        ADMIN, READER
    }
} 