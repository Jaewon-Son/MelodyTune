package com.melodytune.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // 상품명
    private Integer price;      // 가격
    private String imageUrl;    // 이미지 경로 (예: /images/guitar1.jpg)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Builder
    public Product(String name, Integer price, String imageUrl, Store store) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.store = store;
    }
}