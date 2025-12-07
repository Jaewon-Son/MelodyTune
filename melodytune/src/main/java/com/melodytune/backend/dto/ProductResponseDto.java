package com.melodytune.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private Integer price;
    private String imageUrl;
}