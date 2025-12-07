package com.melodytune.backend.repository;

import java.util.Optional;

import com.melodytune.backend.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email); // 이메일로 회원 찾기 (로그인용)
    boolean existsByEmail(String email); // 중복 가입 방지
}