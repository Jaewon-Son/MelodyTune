package com.melodytune.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Set;

@Getter
@NoArgsConstructor
public class StoreRequestDto {
    private String name;        // 가게 이름
    private String region;      // 지역 (예: 강남구)
    private Double lat;         // 위도
    private Double lng;         // 경도
    private Set<String> instruments; // 전문 악기 목록 (예: ["기타", "드럼"])
}