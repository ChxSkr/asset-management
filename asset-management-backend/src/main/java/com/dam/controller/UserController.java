package com.dam.controller;

import com.dam.common.Result;
import com.dam.dto.LoginDTO;
import com.dam.dto.RegisterDTO;
import com.dam.entity.User;
import com.dam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/create")
    public Result<?> createUser(HttpServletRequest request, @RequestBody RegisterDTO dto) {
        var role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限");
        }
        return userService.createUser(dto);
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping("/profile")
    public Result<?> getProfile(HttpServletRequest request) {
        var userId = (Long) request.getAttribute("userId");
        return userService.getProfile(userId);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(HttpServletRequest request, @RequestBody User user) {
        var userId = (Long) request.getAttribute("userId");
        return userService.updateProfile(userId, user);
    }

    @GetMapping("/list")
    public Result<?> getUserList(HttpServletRequest request,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int pageSize) {
        var role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限");
        }
        return userService.getUserList(page, pageSize);
    }

    @GetMapping("/{userId}")
    public Result<?> getUserById(HttpServletRequest request, @PathVariable Long userId) {
        var role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限");
        }
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    public Result<?> updateUser(HttpServletRequest request, @PathVariable Long userId, @RequestBody User user) {
        var role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限");
        }
        return userService.updateUser(userId, user);
    }

    @PutMapping("/{userId}/password")
    public Result<?> resetPassword(HttpServletRequest request, @PathVariable Long userId, @RequestBody Map<String, String> body) {
        var role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限");
        }
        return userService.resetPassword(userId, body.get("newPassword"));
    }
}