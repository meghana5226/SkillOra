package com.skillora.controller;

import com.skillora.dto.session.SessionCreateRequest;
import com.skillora.dto.session.SessionResponse;
import com.skillora.security.UserPrincipal;
import com.skillora.service.SessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody SessionCreateRequest request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sessionService.create(principal.getId(), request));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SessionResponse>> upcoming(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sessionService.upcoming(principal.getId()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SessionResponse>> history(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sessionService.history(principal.getId()));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<SessionResponse> confirm(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sessionService.confirm(principal.getId(), id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<SessionResponse> complete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sessionService.complete(principal.getId(), id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionResponse> cancel(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sessionService.cancel(principal.getId(), id));
    }
}
