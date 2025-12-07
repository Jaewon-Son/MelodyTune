package com.melodytune.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 비밀번호 암호화 도구 (회원가입 시 필요)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 보안 필터 체인 (핵심 설정)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 1. 회원가입, 로그인, 로그인 상태 확인, 로그아웃 등 Auth API는 모두 허용합니다.
                .requestMatchers("/api/auth/**").permitAll()
                
                // 2. 정적 리소스 및 기본 페이지는 모두 허용합니다. (CSS/JS 로딩을 위해 필수)
                .requestMatchers(HttpMethod.GET, "/main.html", "/login.html", "/signup.html", 
                                 "/style.css", "/app.js", "/images/**").permitAll()
                .requestMatchers("/").permitAll() // 메인 페이지 경로

                // 3. GET 요청 (정보 조회)도 허용합니다.
                .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                
                // 4. 나머지 모든 요청 (POST, PUT, DELETE 등)은 인증이 필요합니다.
                .anyRequest().authenticated() 
            );
        return http.build();
    }
}