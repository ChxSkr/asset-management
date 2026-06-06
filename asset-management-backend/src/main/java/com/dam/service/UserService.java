package com.dam.service;

import com.dam.common.Result;
import com.dam.dto.LoginDTO;
import com.dam.dto.RegisterDTO;
import com.dam.entity.User;

public interface UserService {
    Result<?> login(LoginDTO dto);
    Result<?> register(RegisterDTO dto);
    Result<?> createUser(RegisterDTO dto);
    Result<?> getProfile(Long userId);
    Result<?> updateProfile(Long userId, User user);
    Result<?> getUserList(int page, int pageSize);
    Result<?> getUserById(Long userId);
    Result<?> updateUser(Long userId, User user);
    Result<?> resetPassword(Long userId, String newPassword);
}