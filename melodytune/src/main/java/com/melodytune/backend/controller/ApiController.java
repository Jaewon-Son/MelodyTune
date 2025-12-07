package com.melodytune.backend.controller;

import com.melodytune.backend.domain.Member;
import com.melodytune.backend.domain.Product;
import com.melodytune.backend.domain.Store;
import com.melodytune.backend.dto.*;
import com.melodytune.backend.repository.MemberRepository;
import com.melodytune.backend.repository.ProductRepository;
import com.melodytune.backend.repository.StoreRepository;
import com.melodytune.backend.service.AuthService;
import com.melodytune.backend.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.melodytune.backend.dto.ReviewRequestDto;
import com.melodytune.backend.dto.StoreReviewSummaryDto;
import com.melodytune.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final AuthService authService;
    private final StoreService storeService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReviewService reviewService;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    // --- [회원 관련 API] ---

    @PostMapping("/auth/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto dto) {
        authService.signup(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto dto, HttpServletRequest request) {
        // 1. 회원 조회
        Optional<Member> memberOp = memberRepository.findByEmail(dto.getEmail());
        if (memberOp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("존재하지 않는 이메일입니다.");
        }
        Member member = memberOp.get();

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }

        // 3. 세션 생성 (로그인 처리)
        HttpSession session = request.getSession();
        session.setAttribute("loginMember", member); // 세션에 사용자 정보 저장

        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 삭제
        }
        return ResponseEntity.ok("로그아웃 성공");
    }
    // 리뷰 등록
    @PostMapping("/stores/{storeId}/reviews")
    public ResponseEntity<Void> createStoreReview(
            @PathVariable Long storeId,
            // 인증된 사용자 ID를 가져와야 함 (Spring Security 사용)
            @RequestParam Long memberId, 
            @RequestBody ReviewRequestDto request) {

        // 실제 구현 시 memberId는 Principal 객체 등 인증된 정보에서 가져와야 함
        Long reviewId = reviewService.createReview(storeId, memberId, request);

        // 생성된 리뷰의 상세 정보 URI 반환
        return ResponseEntity.created(URI.create("/api/reviews/" + reviewId)).build();
    }

    // 현재 로그인한 사용자 정보 확인 (프론트엔드 UI 제어용)
    @GetMapping("/auth/me")
    public ResponseEntity<?> getMyInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginMember") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 비로그인 상태
        }
        Member member = (Member) session.getAttribute("loginMember");
        // 비밀번호 제외하고 필요한 정보만 리턴 (간단히 이름과 역할만)
        return ResponseEntity.ok().body(new UserInfoDto(member.getName(), member.getRole().name()));
    }
    //리뷰 조회
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<StoreReviewSummaryDto> getStoreReviews(@PathVariable Long storeId) {
        StoreReviewSummaryDto summary = reviewService.getReviewsByStoreId(storeId);
        return ResponseEntity.ok(summary);
    }
    
    // 간단한 UserInfo DTO (내부 클래스)
    @lombok.Getter
    @lombok.AllArgsConstructor
    static class UserInfoDto {
        private String name;
        private String role;
    }

    // --- [악기점 관련 API] ---
    
    @PostMapping("/stores")
    public ResponseEntity<String> registerStore(@RequestBody StoreRequestDto dto, HttpServletRequest request) {
        // ★ 권한 체크: 로그인 했는지? BUSINESS 인지?
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginMember") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        
        Member member = (Member) session.getAttribute("loginMember");
        if (!member.getRole().name().equals("BUSINESS")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("업체 회원만 등록할 수 있습니다.");
        }

        storeService.registerStore(dto, member);
        return ResponseEntity.ok("업체 등록 성공!");
    }

    @GetMapping("/stores")
    public ResponseEntity<List<StoreResponseDto>> searchStores(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String instrument,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(storeService.searchStores(region, instrument, keyword));
    }
    
    @GetMapping("/stores/{storeId}/detail") 
    public ResponseEntity<StoreDetailResponseDto> getStoreDetail(@PathVariable Long storeId) {
        StoreDetailResponseDto response = storeService.getStoreDetail(storeId);
        return ResponseEntity.ok(response);
    }
    
    // --- [비즈니스 기능: 내 매장 관리] ---
    private final String uploadDir = "D:/melodyTune_back/melodytune/product_images/";
    
    // 1. 내 매장 정보 조회 (로그인한 사장님의 매장 찾기)
    @GetMapping("/my-store")
    public ResponseEntity<?> getMyStore(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginMember") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Member member = (Member) session.getAttribute("loginMember");

        // DB에서 내 ID로 등록된 매장 찾기 (StoreRepository에 findByOwnerId 필요)
        Optional<Store> myStore = storeRepository.findByOwner(member);
        
        if (myStore.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content (매장 없음)
        }

        // StoreResponseDto로 변환해서 반환 (기존 DTO 재활용)
        Store store = myStore.get();
        return ResponseEntity.ok(StoreResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .region(store.getRegion())
                .build());
    }

    // 2. 상품 등록 (이미지 업로드 포함)
    @PostMapping("/stores/{storeId}/products")
    public ResponseEntity<String> addProduct(
            @PathVariable Long storeId,
            @RequestParam("name") String name,
            @RequestParam("price") Integer price,
            @RequestParam("image") MultipartFile file
    ) {
        try {
            // 1. 매장 조회
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("매장이 존재하지 않습니다."));

            // 2. 파일 저장 로직
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename(); // 파일명 중복 방지
            File dest = new File(uploadDir + fileName);
            
            // 디렉토리가 없으면 생성
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            
            file.transferTo(dest); // 실제 파일 저장

            // 3. DB에 상품 정보 저장
            // 웹 접근 경로: /images/파일명
            String fileUrl = "/images/" + fileName; 
            
            Product product = Product.builder()
                    .store(store)
                    .name(name)
                    .price(price)
                    .imageUrl(fileUrl)
                    .build();
            
            productRepository.save(product);

            return ResponseEntity.ok("상품 등록 완료");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }
    }
 // 3. 상품 삭제 API
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId, HttpServletRequest request) {
        // 1. 로그인 체크
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginMember") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Member member = (Member) session.getAttribute("loginMember");

        // 2. 상품 조회
        Optional<Product> productOp = productRepository.findById(productId);
        if (productOp.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품이 존재하지 않습니다.");
        }
        Product product = productOp.get();

        // 3. 권한 체크 (이 상품의 가게 주인이 나인지?)
        if (!product.getStore().getOwner().getId().equals(member.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 매장 상품만 삭제할 수 있습니다.");
        }

        // 4. 삭제 수행
        // (선택사항: 실제 파일 삭제 로직을 넣으려면 여기서 uploadDir + product.getImageUrl() 파싱해서 File.delete() 수행)
        productRepository.delete(product);

        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }
    
}