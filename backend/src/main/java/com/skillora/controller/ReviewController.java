package com.skillora.controller;

import com.skillora.dto.review.ReviewCreateRequest;
import com.skillora.dto.review.ReviewResponse;
import com.skillora.security.UserPrincipal;
import com.skillora.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/reviews")
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewCreateRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reviewService.create(principal.getId(), request));
    }

    @GetMapping("/api/users/{id}/reviews")
    public ResponseEntity<Page<ReviewResponse>> forUser(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.forUser(id, PageRequest.of(page, size)));
    }
}
