package com.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers("/api/users/*", "/api/users/*/approve", "/api/users/*/reject").permitAll()
                        .requestMatchers("/api/users/*/assign-role", "/api/users/*/remove-role").permitAll()
                        .requestMatchers("/api/users/*/apply-volunteer", "/api/users/*/approve-volunteer", "/api/users/*/cancel-volunteer").permitAll()
                        .requestMatchers("/api/users/*/apply-adopter", "/api/users/*/approve-adopter", "/api/users/*/cancel-adopter").permitAll()
                        .requestMatchers("/api/users/*/approve-delete", "/api/users/*/reject-delete").permitAll()
                        .requestMatchers("/api/users/volunteers", "/api/users/pending-delete").permitAll()
                        .requestMatchers("/api/users/volunteer-applications").permitAll()
                        .requestMatchers("/api/users/*/volunteer-stats", "/api/users/*/service-records", "/api/users/*/registrations").permitAll()
                        .requestMatchers("/api/volunteer/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/admin/verify").permitAll()
                        .requestMatchers("/api/users/pending").permitAll()
                        .requestMatchers("/api/users/admin/all-users").permitAll()
                        .requestMatchers("/api/users/volunteers").permitAll()
                        .requestMatchers("/api/users/pending-delete").permitAll()
                        .requestMatchers("/api/users/{id}").permitAll()
                        .requestMatchers("/api/animals/**").permitAll()
                        .requestMatchers("/api/surrender/**").permitAll()
                        .requestMatchers("/api/adoption-applications/**").permitAll()
                        .requestMatchers("/api/adoption-visits/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5174"));
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