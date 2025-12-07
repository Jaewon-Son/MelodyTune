package com.melodytune.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 로그인 ID로 사용

    @Column(nullable = false)
    private String password; // 암호화되어 저장됨

    @Column(nullable = false)
    private String name; // 사용자 이름 or 업체명

    @Enumerated(EnumType.STRING)
    private Role role; // USER 또는 BUSINESS
}