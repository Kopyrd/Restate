package com.example.restate.service;

import com.example.restate.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;


import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User save(User user);
    User update(Long id, User user);
    void deleteById(Long id);
    User registerUser(User user);
    User createAdmin(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}