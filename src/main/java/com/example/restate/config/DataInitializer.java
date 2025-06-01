package com.example.restate.config;

import com.example.restate.entity.Role;
import com.example.restate.entity.User;
import com.example.restate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Sprawdź czy admin już istnieje
        if (!userService.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("password123"); // Zostanie automatycznie zahashowane
            admin.setEmail("admin@restate.pl");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            
            userService.save(admin);
        }

        if (!userService.existsByUsername("user1")) {
            User user = new User();
            user.setUsername("user1");
            user.setPassword("password123"); // Zostanie automatycznie zahashowane
            user.setEmail("user1@restate.pl");
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setRole(Role.USER);
            user.setEnabled(true);
            
            userService.save(user);
        }
    }
}