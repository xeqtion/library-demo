package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.AdminRegisterDTO;
import com.manage.librarydemo.dto.LoginRequest;
import com.manage.librarydemo.dto.LoginResponse;
import com.manage.librarydemo.dto.UserDTO;
import com.manage.librarydemo.security.JwtUtils;
import com.manage.librarydemo.service.UserService;
import com.manage.librarydemo.config.AppProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody UserDTO userDTO) {
        // 对密码进行加密
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        // 默认为读者角色
        userDTO.setRole("READER");
        
        // 默认启用账户
        userDTO.setEnabled(true);
        
        return Result.success(userService.add(userDTO));
    }
    
    /**
     * 管理员注册接口
     * 需要提供管理员注册码
     */
    @PostMapping("/register/admin")
    public Result<UserDTO> registerAdmin(@Valid @RequestBody AdminRegisterDTO adminRegisterDTO) {
        // 验证管理员注册码
        if (!appProperties.getAdmin().getRegisterCode().equals(adminRegisterDTO.getAdminCode())) {
            return Result.error(400, "管理员注册码不正确");
        }
        
        // 创建用户DTO
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(adminRegisterDTO.getUsername());
        userDTO.setPassword(passwordEncoder.encode(adminRegisterDTO.getPassword()));
        userDTO.setName(adminRegisterDTO.getName());
        userDTO.setPhone(adminRegisterDTO.getPhone());
        userDTO.setEmail(adminRegisterDTO.getEmail());
        
        // 设置为管理员角色
        userDTO.setRole("ADMIN");
        
        // 启用账户
        userDTO.setEnabled(true);
        
        return Result.success(userService.add(userDTO));
    }

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