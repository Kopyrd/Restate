/*
package com.example.restate.controller;

import com.example.restate.entity.User;
import com.example.restate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/test-hash")
    public ResponseEntity<String> testHash() {
        String hash = "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6";
        boolean matches = passwordEncoder.matches("password123", hash);
        String newHash = passwordEncoder.encode("password123");

        return ResponseEntity.ok("Pasuje: " + matches + "\nNowy hash: " + newHash);
    }

    @GetMapping("/check-user/{username}")
    public ResponseEntity<String> checkUser(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        if (exists) {
            User user = userService.findByUsername(username).get();
            return ResponseEntity.ok("User exists. Role: " + user.getRole() + ", Enabled: " + user.isEnabled());
        }
        return ResponseEntity.ok("User does not exist");
    }

    @GetMapping("/test-hash/{username}")
    public ResponseEntity<String> testHashForUser(@PathVariable String username) {
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok("User not found");
        }

        User user = userOpt.get();
        String hashFromDb = user.getPassword();
        boolean matches = passwordEncoder.matches("password123", hashFromDb);

        return ResponseEntity.ok(
                "Username: " + username +
                        "\nHash from DB: " + hashFromDb +
                        "\nPassword matches: " + matches
        );
    }
}
*/