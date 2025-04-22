package com.manage.librarydemo.service.impl;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.dto.UserDTO;
import com.manage.librarydemo.entity.Borrowing;
import com.manage.librarydemo.entity.User;
import com.manage.librarydemo.repository.BorrowingRepository;
import com.manage.librarydemo.repository.UserRepository;
import com.manage.librarydemo.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BorrowingRepository borrowingRepository;

    @Override
    public PageResult<UserDTO> getPage(com.manage.librarydemo.common.PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.getPageNum() - 1,
                pageRequest.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Page<User> page;
        if (StringUtils.hasText(pageRequest.getKeyword())) {
            page = userRepository.findByKeyword(pageRequest.getKeyword(), pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        return PageResult.of(
                page.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                page.getTotalElements(),
                pageRequest.getPageNum(),
                pageRequest.getPageSize()
        );
    }

    @Override
    public UserDTO getById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    }

    @Override
    public UserDTO getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    }

    @Override
    @Transactional
    public UserDTO add(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        // 处理角色
        if (userDTO.getRole() != null) {
            try {
                user.setRole(User.UserRole.valueOf(userDTO.getRole()));
            } catch (IllegalArgumentException e) {
                user.setRole(User.UserRole.READER); // 默认为读者
            }
        } else {
            user.setRole(User.UserRole.READER);
        }
        
        // 默认启用
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        
        // 确保createTime不为null
        if (user.getCreateTime() == null) {
            user.setCreateTime(java.time.LocalDateTime.now());
        }

        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO update(UserDTO userDTO) {
        User user = findUserEntity(userDTO.getId());
        
        // 如果修改了用户名，检查是否重复
        if (!user.getUsername().equals(userDTO.getUsername()) && 
                userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 如果密码为空，保持原密码
        if (!StringUtils.hasText(userDTO.getPassword())) {
            userDTO.setPassword(user.getPassword());
        }
        
        // 保存旧的角色
        User.UserRole oldRole = user.getRole();
        
        // 保存原有的创建时间
        LocalDateTime oldCreateTime = user.getCreateTime();
        
        BeanUtils.copyProperties(userDTO, user);
        
        // 处理角色
        if (userDTO.getRole() != null) {
            try {
                user.setRole(User.UserRole.valueOf(userDTO.getRole()));
            } catch (IllegalArgumentException e) {
                user.setRole(oldRole); // 如果转换失败，保持原角色
            }
        } else {
            user.setRole(oldRole);
        }
        
        // 确保createTime不丢失
        if (user.getCreateTime() == null) {
            if (oldCreateTime != null) {
                user.setCreateTime(oldCreateTime);
            } else {
                user.setCreateTime(java.time.LocalDateTime.now());
            }
        }
        
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查该用户是否有借阅记录
        if (hasBorrowingRecords(id)) {
            throw new IllegalStateException("该用户有未完成的借阅记录，无法删除");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User findUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    }

    @Override
    public boolean verifyPassword(Long userId, String password) {
        User user = findUserEntity(userId);
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = findUserEntity(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * 检查用户是否有借阅记录
     * @param userId 用户ID
     * @return 是否有借阅记录
     */
    private boolean hasBorrowingRecords(Long userId) {
        // 从borrowingRepository查询用户的借阅记录数量
        return borrowingRepository.countByUserIdAndStatusIn(
                userId,
                Arrays.asList(
                    Borrowing.BorrowingStatus.PENDING,
                    Borrowing.BorrowingStatus.BORROWED,
                    Borrowing.BorrowingStatus.OVERDUE
                )
        ) > 0;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        // 设置角色为字符串
        dto.setRole(user.getRole().name());
        // 不返回密码
        dto.setPassword(null);
        return dto;
    }
} 