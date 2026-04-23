package com.example.lipsticks.config;

import com.example.lipsticks.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/error", "/h2-console/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mall/products", "/api/mall/products/*", "/api/mall/products/page")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mall/brands", "/api/mall/categories")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recommend")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/recommend/ai")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/visualization/**")
                        .permitAll()
                        .requestMatchers("/api/home/**")
                        .permitAll()
                        .requestMatchers("/api/tryon/**")
                        .authenticated()
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
