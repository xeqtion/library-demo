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
} 