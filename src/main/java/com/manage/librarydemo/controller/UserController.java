package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.PasswordChangeDTO;
import com.manage.librarydemo.dto.UserDTO;
import com.manage.librarydemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<UserDTO>> getPage(PageRequest pageRequest) {
        return Result.success(userService.getPage(pageRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or authentication.principal.username == @userService.getById(#id).username")
    public Result<UserDTO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result<UserDTO> getByUsername(@PathVariable String username) {
        return Result.success(userService.getByUsername(username));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<UserDTO> add(@Valid @RequestBody UserDTO userDTO) {
        // 管理员可以创建任何角色的用户
        // 对密码进行加密
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return Result.success(userService.add(userDTO));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN') or authentication.principal.username == @userService.getById(#userDTO.id).username")
    public Result<UserDTO> update(@Valid @RequestBody UserDTO userDTO) {
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        
        // 如果不是管理员，则不允许修改角色
        if (!isAdmin) {
            UserDTO existingUser = userService.getById(userDTO.getId());
            userDTO.setRole(existingUser.getRole());
            
            // 如果密码不为空，则加密密码
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
        } else if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            // 管理员修改，如果密码不为空，则加密密码
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        return Result.success(userService.update(userDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
    
    /**
     * 修改当前用户个人信息
     */
    @PutMapping("/profile")
    public Result<UserDTO> updateProfile(@Valid @RequestBody UserDTO userDTO) {
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO currentUser = userService.getByUsername(username);
        
        // 设置ID，防止篡改
        userDTO.setId(currentUser.getId());
        // 不允许修改用户名和角色
        userDTO.setUsername(currentUser.getUsername());
        userDTO.setRole(currentUser.getRole());
        
        // 如果密码为空，则不修改密码
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            userDTO.setPassword(null);
        } else {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        return Result.success(userService.update(userDTO));
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO currentUser = userService.getByUsername(username);
        
        // 验证旧密码
        if (!userService.verifyPassword(currentUser.getId(), passwordChangeDTO.getOldPassword())) {
            return Result.error(400, "旧密码不正确");
        }
        
        // 修改密码
        userService.changePassword(currentUser.getId(), passwordChangeDTO.getNewPassword());
        
        return Result.success();
    }
} 