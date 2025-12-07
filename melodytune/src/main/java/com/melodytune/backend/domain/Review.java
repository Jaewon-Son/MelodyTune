package com.melodytune.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 리뷰 대상 악기점

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 리뷰 작성자

    @Column(nullable = false)
    private Double rating; // 별점 (1.0 ~ 5.0)

    @Lob
    @Column(nullable = false)
    private String content; // 리뷰 내용

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 일자

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}