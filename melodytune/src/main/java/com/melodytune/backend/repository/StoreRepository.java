package com.melodytune.backend.repository;

import com.melodytune.backend.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.melodytune.backend.domain.Member;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
	Optional<Store> findByOwner(Member owner);
	
	// 통합 검색 쿼리
	@Query("SELECT DISTINCT s FROM Store s " +
	           "LEFT JOIN s.instruments i " +
	           "LEFT JOIN s.products p " +
	           "WHERE s.name LIKE %:keyword% " +
	           "OR s.address LIKE %:keyword% " +
	           "OR s.description LIKE %:keyword% " +
	           "OR i LIKE %:keyword% " +
	           "OR p.name LIKE %:keyword%")
	    List<Store> findByKeyword(@Param("keyword") String keyword);
	
	List<Store> findByRegionAndInstrumentsIn(String region, java.util.Collection<String> instruments);
    List<Store> findByRegion(String region);
    List<Store> findByInstrumentsIn(java.util.Collection<String> instruments);
    
}