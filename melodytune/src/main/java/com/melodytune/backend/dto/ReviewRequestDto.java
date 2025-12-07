package com.melodytune.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {
    private Double rating; // 별점
    private String content; // 리뷰 내용
}