package com.manage.librarydemo.service;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.dto.UserDTO;
import com.manage.librarydemo.entity.User;

public interface UserService {

    PageResult<UserDTO> getPage(PageRequest pageRequest);

    UserDTO getById(Long id);

    UserDTO getByUsername(String username);

    UserDTO add(UserDTO userDTO);

    UserDTO update(UserDTO userDTO);

    void delete(Long id);

    User findUserEntity(Long id);
    
    /**
     * 验证用户密码
     * @param userId 用户ID
     * @param password 密码
     * @return 是否验证通过
     */
    boolean verifyPassword(Long userId, String password);
    
    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String newPassword);
} 