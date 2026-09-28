package com.skillora.controller;

import com.skillora.dto.swap.SwapRequestCreateRequest;
import com.skillora.dto.swap.SwapRequestResponse;
import com.skillora.security.UserPrincipal;
import com.skillora.service.SwapRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/swaps")
@RequiredArgsConstructor
@Tag(name = "Swap Requests")
public class SwapRequestController {

    private final SwapRequestService swapRequestService;

    @PostMapping
    public ResponseEntity<SwapRequestResponse> create(@Valid @RequestBody SwapRequestCreateRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(swapRequestService.create(principal.getId(), request));
    }

    @GetMapping("/sent")
    public ResponseEntity<Page<SwapRequestResponse>> sent(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(swapRequestService.sent(principal.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/received")
    public ResponseEntity<Page<SwapRequestResponse>> received(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(swapRequestService.received(principal.getId(), PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<SwapRequestResponse> accept(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(swapRequestService.accept(principal.getId(), id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<SwapRequestResponse> reject(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(swapRequestService.reject(principal.getId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SwapRequestResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(swapRequestService.cancel(principal.getId(), id));
    }
}
