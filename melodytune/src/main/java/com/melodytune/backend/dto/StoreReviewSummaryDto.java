package com.melodytune.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class StoreReviewSummaryDto {
    private String storeName;
    private Double averageRating;
    private Long totalReviews;
    private List<String> aiSummary;
    private List<ReviewResponseDto> reviews;
}