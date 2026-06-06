package com.dam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dam.common.Result;
import com.dam.dto.LoginDTO;
import com.dam.dto.RegisterDTO;
import com.dam.entity.User;
import com.dam.mapper.UserMapper;
import com.dam.service.UserService;
import com.dam.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Result<?> login(LoginDTO dto) {
        var user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        var token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("realName", user.getRealName());
        return Result.ok("登录成功", data);
    }

    @Override
    public Result<?> register(RegisterDTO dto) {
        var existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        var user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole("user");
        user.setStatus(1);
        userMapper.insert(user);
        return Result.ok("注册成功");
    }

    @Override
    public Result<?> createUser(RegisterDTO dto) {
        var existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        var user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole() != null ? dto.getRole() : "user");
        user.setStatus(1);
        userMapper.insert(user);
        return Result.ok("创建用户成功");
    }

    @Override
    public Result<?> getProfile(Long userId) {
        var user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.ok(user);
    }

    @Override
    public Result<?> updateProfile(Long userId, User user) {
        var existUser = userMapper.selectById(userId);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        user.setUserId(userId);
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);
        userMapper.updateById(user);
        return Result.ok("更新成功");
    }

    @Override
    public Result<?> getUserList(int page, int pageSize) {
        var pageResult = userMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        pageResult.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(pageResult);
    }

    @Override
    public Result<?> getUserById(Long userId) {
        var user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.ok(user);
    }

    @Override
    public Result<?> updateUser(Long userId, User user) {
        var existUser = userMapper.selectById(userId);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        user.setUserId(userId);
        user.setUsername(null);
        user.setPassword(null);
        userMapper.updateById(user);
        return Result.ok("更新成功");
    }

    @Override
    public Result<?> resetPassword(Long userId, String newPassword) {
        var existUser = userMapper.selectById(userId);
        if (existUser == null) {
            return Result.error("用户不存在");
        }
        existUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(existUser);
        return Result.ok("重置密码成功");
    }
}