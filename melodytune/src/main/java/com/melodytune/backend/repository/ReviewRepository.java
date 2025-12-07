package com.melodytune.backend.repository;

import com.melodytune.backend.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 특정 업체(storeId)의 리뷰 목록을 최신순으로 조회
    List<Review> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    // 특정 업체(storeId)의 리뷰 평균 별점 계산
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.store.id = :storeId")
    Double findAverageRatingByStoreId(@Param("storeId") Long storeId);

    // 특정 업체(storeId)의 리뷰 개수 계산
    Long countByStoreId(Long storeId);
}