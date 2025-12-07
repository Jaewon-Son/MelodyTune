package com.melodytune.backend.service;

import com.melodytune.backend.domain.Member;
import com.melodytune.backend.domain.Store;
import com.melodytune.backend.dto.ProductResponseDto;
import com.melodytune.backend.dto.StoreDetailResponseDto;
import com.melodytune.backend.dto.StoreRequestDto;
import com.melodytune.backend.dto.StoreResponseDto;
import com.melodytune.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    // 악기점 등록 (업체 회원만 가능하도록 컨트롤러에서 제어)
    @Transactional
    public void registerStore(StoreRequestDto dto, Member owner) {

        Store store = Store.builder()
                .name(dto.getName())
                .region(dto.getRegion())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .instruments(dto.getInstruments())
                .owner(owner) // 추후 로그인 연동 시 추가
                .build();
        
        storeRepository.save(store);
    }

    // 악기점 검색 (모두 가능)
    @Transactional(readOnly = true)
    public List<StoreResponseDto> searchStores(String region, String instrument, String keyword) {
        List<Store> stores;
        
     // 1. 키워드 검색이 최우선
        if (keyword != null && !keyword.isBlank()) {
            stores = storeRepository.findByKeyword(keyword);
        } 
        // 2. 기존 필터 로직 (지역/악기)
        else if (region != null && instrument != null) {
            stores = storeRepository.findByRegionAndInstrumentsIn(region, Set.of(instrument));
        } else if (region != null) {
            stores = storeRepository.findByRegion(region);
        } else if (instrument != null) {
            stores = storeRepository.findByInstrumentsIn(Set.of(instrument));
        } else {
            stores = storeRepository.findAll();
        }

        // 악기 필터링 
        return stores.stream()
                .filter(s -> instrument == null || instrument.equals("전체 악기") || s.getInstruments().contains(instrument))
                .map(StoreResponseDto::from) // Entity -> DTO 변환
                .collect(Collectors.toList());
    }
    
    // 업체 상세 정보 조회
    public StoreDetailResponseDto getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 업체를 찾을 수 없습니다."));

        // Product 엔티티 -> DTO 변환
        List<ProductResponseDto> productDtos = store.getProducts().stream()
                .map(product -> ProductResponseDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .imageUrl(product.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        return StoreDetailResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .region(store.getRegion())
                .instruments(store.getInstruments())
                .averageRating(store.getAverageRating() != null ? store.getAverageRating() : 0.0)
                .totalReviews(store.getTotalReviews() != null ? store.getTotalReviews() : 0L)
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .operatingHours(store.getOperatingHours())
                .description(store.getDescription())
                .products(productDtos)
                .build();
    }
}