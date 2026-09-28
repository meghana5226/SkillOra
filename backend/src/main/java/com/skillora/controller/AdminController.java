package com.skillora.controller;

import com.skillora.dto.admin.AdminDashboardResponse;
import com.skillora.dto.admin.AdminUserStatusRequest;
import com.skillora.dto.user.UserResponse;
import com.skillora.security.UserPrincipal;
import com.skillora.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> users(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.listUsers(PageRequest.of(page, size)));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> setStatus(@PathVariable Long id, @Valid @RequestBody AdminUserStatusRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(adminService.setUserActive(principal.getId(), id, request.getActive()));
    }
}
