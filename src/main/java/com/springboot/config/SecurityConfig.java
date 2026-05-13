package com.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/api/users/login", "/api/users/admin/verify").permitAll()
                        .requestMatchers("/api/users/pending", "/api/users/admin/all-users").permitAll()
                        .requestMatchers("/api/users/{id}", "/api/users/{id}/approve", "/api/users/{id}/reject").permitAll()
                        .requestMatchers("/api/users/{id}/assign-role", "/api/users/{id}/remove-role").permitAll()
                        .requestMatchers("/api/users/{id}/apply-volunteer", "/api/users/{id}/approve-volunteer", "/api/users/{id}/cancel-volunteer").permitAll()
                        .requestMatchers("/api/users/{id}/apply-adopter", "/api/users/{id}/approve-adopter", "/api/users/{id}/cancel-adopter").permitAll()
                        .requestMatchers("/api/users/{id}/approve-delete", "/api/users/{id}/reject-delete").permitAll()
                        .requestMatchers("/api/users/volunteers", "/api/users/pending-delete").permitAll()
                        .requestMatchers("/api/fundraising/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
