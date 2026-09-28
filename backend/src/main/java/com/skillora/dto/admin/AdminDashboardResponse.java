package com.skillora.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalSkills;
    private long activeSwaps;
    private long completedSessions;
    private double averageRating;
    private long pendingSwaps;
    private long recentRegistrations7d;
}
