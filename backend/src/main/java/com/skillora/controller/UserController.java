package com.skillora.controller;

import com.skillora.dto.user.*;
import com.skillora.security.UserPrincipal;
import com.skillora.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> discover(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.discover(query, principal.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFullProfile(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UserUpdateRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.updateProfile(principal.getId(), request));
    }

    @PostMapping("/me/offered-skills")
    public ResponseEntity<UserResponse> addOfferedSkill(@Valid @RequestBody AddOfferedSkillRequest request,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.addOfferedSkill(principal.getId(), request));
    }

    @PostMapping("/me/wanted-skills")
    public ResponseEntity<UserResponse> addWantedSkill(@Valid @RequestBody AddWantedSkillRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.addWantedSkill(principal.getId(), request));
    }

    @DeleteMapping("/me/offered-skills/{id}")
    public ResponseEntity<Void> removeOfferedSkill(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        userService.removeOfferedSkill(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/wanted-skills/{id}")
    public ResponseEntity<Void> removeWantedSkill(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        userService.removeWantedSkill(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
