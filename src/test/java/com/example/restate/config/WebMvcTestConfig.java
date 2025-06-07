package com.example.restate.config;

import com.example.restate.security.JwtAuthenticationFilter;
import com.example.restate.service.UserService;
import com.example.restate.util.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class WebMvcTestConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        // Use a real JwtAuthenticationFilter with mocked dependencies
        return new JwtAuthenticationFilter(jwtUtil(), userDetailsService());
    }

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return mock(JwtUtil.class);
    }

    @Bean
    @Primary
    public UserService userService() {
        return mock(UserService.class);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return mock(UserDetailsService.class);
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager() {
        return mock(AuthenticationManager.class);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
