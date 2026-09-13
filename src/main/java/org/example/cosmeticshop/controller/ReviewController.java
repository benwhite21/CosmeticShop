package org.example.cosmeticshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cosmeticshop.dto.ReviewRequest;
import org.example.cosmeticshop.entity.Review;
import org.example.cosmeticshop.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Controller", description = "Quản lý đánh giá và nhận xét sản phẩm")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Thêm đánh giá mới cho sản phẩm")
    @PostMapping
    public ResponseEntity<?> addReview(@RequestParam Long userId,
                                       @RequestParam Long productId,
                                       @RequestBody ReviewRequest request) {
        try {
            Review review = reviewService.addReview(userId, productId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Lấy danh sách tất cả đánh giá của một sản phẩm")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }
}