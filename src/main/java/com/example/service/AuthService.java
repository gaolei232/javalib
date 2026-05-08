package com.example.service;

import com.example.entity.User;

import java.util.Map;

public interface AuthService {

    Map<String, Object> login(String email, String password);

    Map<String, Object> register(String name, String email, String password);

    Map<String, Object> logout();

    Map<String, Object> getCurrentUser(Long userId);

    Map<String, Object> getCurrentUserByToken(String authorizationHeader);

    Map<String, Object> refreshToken(String token);

    Map<String, Object> forgotPassword(String email);

    Map<String, Object> resetPassword(String token, String password);

    Map<String, Object> changePassword(String authorizationHeader, String oldPassword, String newPassword);

}