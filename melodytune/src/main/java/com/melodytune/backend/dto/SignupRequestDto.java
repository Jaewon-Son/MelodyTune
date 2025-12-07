package com.melodytune.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {
    private String email;       // 아이디 (이메일)
    private String password;    
    private String name;        // 사용자 이름 (또는 업체명)
    private boolean businessUser; // 업체 회원 여부 (true면 사장님, false면 일반)
}