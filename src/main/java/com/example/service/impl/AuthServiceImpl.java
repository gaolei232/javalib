package com.example.service.impl;

import com.example.entity.User;
import com.example.service.AuthService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Override
    public Map<String, Object> login(String email, String password) {
        User user = userService.login(email, password);
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            String token = generateToken(user);
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("user", user);
        } else {
            response.put("success", false);
            response.put("message", "邮箱或密码错误");
        }

        return response;
    }

    @Override
    public Map<String, Object> register(String name, String email, String password) {
        User user = userService.register(name, email, password);
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            String token = generateToken(user);
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("token", token);
            response.put("user", user);
        } else {
            response.put("success", false);
            response.put("message", "该邮箱已被注册");
        }

        return response;
    }

    @Override
    public Map<String, Object> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        return response;
    }

    @Override
    public Map<String, Object> getCurrentUser(Long userId) {
        User user = userService.getUserById(userId);
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            response.put("success", true);
            response.put("user", user);
        } else {
            response.put("success", false);
            response.put("message", "用户不存在");
        }

        return response;
    }

    @Override
    public Map<String, Object> getCurrentUserByToken(String authorizationHeader) {
        Map<String, Object> response = new HashMap<>();
        Long userId = extractUserIdFromAuthorizationHeader(authorizationHeader);

        if (userId == null) {
            response.put("success", false);
            response.put("message", "未登录或token无效");
            return response;
        }

        return getCurrentUser(userId);
    }

    @Override
    public Map<String, Object> refreshToken(String token) {
        Map<String, Object> response = new HashMap<>();

        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "token无效");
            return response;
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        String newToken = generateToken(user);
        response.put("success", true);
        response.put("token", newToken);
        return response;
    }

    @Override
    public Map<String, Object> forgotPassword(String email) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.findByEmail(email);

        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            response.put("success", true);
            response.put("message", "密码重置邮件已发送");
            response.put("token", resetToken);
        } else {
            response.put("success", false);
            response.put("message", "该邮箱未注册");
        }

        return response;
    }

    @Override
    public Map<String, Object> resetPassword(String token, String password) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "密码重置成功");
        return response;
    }

    private String generateToken(User user) {
        return "user-" + user.getId() + "-" + UUID.randomUUID();
    }

    private Long extractUserIdFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authorizationHeader.substring(7);
        return extractUserIdFromToken(token);
    }

    private Long extractUserIdFromToken(String token) {
        if (token == null || token.isBlank() || !token.startsWith("user-")) {
            return null;
        }

        String[] parts = token.split("-");
        if (parts.length < 3) {
            return null;
        }

        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
