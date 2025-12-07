package com.melodytune.backend.service;

import com.melodytune.backend.domain.Member;
import com.melodytune.backend.domain.Role;
import com.melodytune.backend.dto.SignupRequestDto;
import com.melodytune.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequestDto dto) {
        // 1. 이메일 중복 검사
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        // 2. 비밀번호 암호화 (SecurityConfig 필요)
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        
        // 3. 역할(Role) 설정 (프론트에서 businessUser=true로 보내면 BUSINESS 권한 부여)
        Role role = dto.isBusinessUser() ? Role.BUSINESS : Role.USER;

        // 4. Member 엔티티 생성 (Builder 패턴 사용)
        Member member = Member.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .name(dto.getName())
                .role(role)
                .build();

        // 5. DB 저장
        memberRepository.save(member);
    }
}