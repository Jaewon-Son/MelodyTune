package com.melodytune.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class StoreDetailResponseDto {
    private Long id;
    private String name;
    private String region;
    private Set<String> instruments;
    private Double averageRating;
    private Long totalReviews;
    
    // 상세 정보
    private String address;
    private String phoneNumber;
    private String operatingHours;
    private String description;
    
    // 판매 상품 목록
    private List<ProductResponseDto> products;
}