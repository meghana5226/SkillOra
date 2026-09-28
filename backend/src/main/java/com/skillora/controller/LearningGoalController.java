package com.skillora.controller;

import com.skillora.dto.goal.LearningGoalRequest;
import com.skillora.dto.goal.LearningGoalResponse;
import com.skillora.security.UserPrincipal;
import com.skillora.service.LearningGoalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Learning Goals")
public class LearningGoalController {

    private final LearningGoalService learningGoalService;

    @GetMapping
    public ResponseEntity<List<LearningGoalResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(learningGoalService.list(principal.getId()));
    }

    @PostMapping
    public ResponseEntity<LearningGoalResponse> create(@Valid @RequestBody LearningGoalRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(learningGoalService.create(principal.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearningGoalResponse> update(@PathVariable Long id, @RequestBody LearningGoalRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(learningGoalService.update(principal.getId(), id, request));
    }
}
