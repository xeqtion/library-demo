package com.manage.librarydemo.service.impl;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.dto.UserDTO;
import com.manage.librarydemo.entity.User;
import com.manage.librarydemo.repository.UserRepository;
import com.manage.librarydemo.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public PageResult<UserDTO> getPage(com.manage.librarydemo.common.PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest pageable = PageRequest.of(
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
        
        // 默认为读者
        if (user.getRole() == null) {
            user.setRole(User.UserRole.READER);
        }
        
        // 默认启用
        if (user.getEnabled() == null) {
            user.setEnabled(true);
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
        
        BeanUtils.copyProperties(userDTO, user);
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        // 不返回密码
        dto.setPassword(null);
        return dto;
    }
} 