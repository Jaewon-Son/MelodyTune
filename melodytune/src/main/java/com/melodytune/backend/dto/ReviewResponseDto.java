package com.melodytune.backend.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private Double rating;
    private String content;
    private String userName; // 작성자 이름
    private LocalDateTime createdAt;
}