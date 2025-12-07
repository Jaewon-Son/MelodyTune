package com.melodytune.backend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 가게 이름

    private String region; // 지역 (예: "강남구")

    private Double lat; // 위도
    private Double lng; // 경도

    //Builder 사용 시 초기값(0.0)이 무시되지 않도록 Default 설정
    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Long totalReviews = 0L;

    //상세 정보 필드 추가
    private String address;       // 상세 주소 (예: "천안시 동남구 신부동 123")
    private String phoneNumber;   // 전화번호 (예: "041-123-4567")
    private String operatingHours;// 운영 시간 (예: "09:00 ~ 21:00")
    
    @Column(columnDefinition = "TEXT") // 긴 텍스트 저장을 위해 TEXT 타입 지정
    private String description;   // 매장 소개글

    @ElementCollection 
    private Set<String> instruments; // 전문 악기 목록

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner; // 이 가게를 등록한 사장님 (Business User)


    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 빈 리스트로 초기화 보장
    private List<Product> products = new ArrayList<>();

    // 통계 업데이트 편의 메서드
    public void updateStatistics(Double averageRating, Long totalReviews) {
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }
}