package com.melodytune.backend.dto;

import lombok.Builder;
import com.melodytune.backend.domain.Store;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class StoreResponseDto {
    private Long id;
    private String name;
    private String region;
    private Double lat;
    private Double lng;
    private Set<String> instruments;

    // Entity(Store) -> DTO(StoreResponseDto) 변환 메서드
    public static StoreResponseDto from(Store store) {
        return StoreResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .region(store.getRegion())
                .lat(store.getLat())
                .lng(store.getLng())
                .instruments(store.getInstruments())
                .build();
    }
    
}