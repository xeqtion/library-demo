package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.LoginRequest;
import com.manage.librarydemo.dto.LoginResponse;
import com.manage.librarydemo.dto.UserDTO;
import com.manage.librarydemo.security.JwtUtils;
import com.manage.librarydemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // 认证用户名和密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 获取用户信息并生成Token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);
        
        // 获取用户详细信息
        UserDTO userDTO = userService.getByUsername(userDetails.getUsername());
        
        // 创建响应对象
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setId(userDTO.getId());
        loginResponse.setUsername(userDTO.getUsername());
        loginResponse.setName(userDTO.getName());
        loginResponse.setEmail(userDTO.getEmail());
        loginResponse.setRole(userDTO.getRole());
        
        return Result.success(loginResponse);
    }
    
    @GetMapping("/info")
    public Result<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserDTO userDTO = userService.getByUsername(userDetails.getUsername());
        return Result.success(userDTO);
    }
} 