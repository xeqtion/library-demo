package com.manage.librarydemo.dto;

import com.manage.librarydemo.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    
    private Long id;
    
    private String username;
    
    private String name;
    
    private String email;
    
    private String role;
    
    private String token;
} 