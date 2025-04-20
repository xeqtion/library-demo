package com.manage.librarydemo.controller;

import com.manage.librarydemo.common.PageRequest;
import com.manage.librarydemo.common.PageResult;
import com.manage.librarydemo.common.Result;
import com.manage.librarydemo.dto.UserDTO;
import com.manage.librarydemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    public Result<PageResult<UserDTO>> getPage(PageRequest pageRequest) {
        return Result.success(userService.getPage(pageRequest));
    }

    @GetMapping("/{id}")
    public Result<UserDTO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result<UserDTO> getByUsername(@PathVariable String username) {
        return Result.success(userService.getByUsername(username));
    }

    @PostMapping
    public Result<UserDTO> add(@Valid @RequestBody UserDTO userDTO) {
        return Result.success(userService.add(userDTO));
    }

    @PutMapping
    public Result<UserDTO> update(@Valid @RequestBody UserDTO userDTO) {
        return Result.success(userService.update(userDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
} 