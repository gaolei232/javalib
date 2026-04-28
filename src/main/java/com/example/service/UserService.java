package com.example.service;

import com.example.entity.User;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    User findByEmail(String email);

    User login(String email, String password);

    User register(String name, String email, String password);

}