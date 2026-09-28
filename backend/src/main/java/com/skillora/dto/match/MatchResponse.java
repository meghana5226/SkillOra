package com.skillora.dto.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private Long userId;
    private String name;
    private String profileImage;
    private String location;
    private Double averageRating;
    private Integer matchPercentage;

    /** Skills this candidate could teach the current user (candidate offers, current user wants). */
    private List<String> theyCanTeachYou;

    /** Skills the current user could teach this candidate (current user offers, candidate wants). */
    private List<String> youCanTeachThem;

    private String explanation;

    // score breakdown, for transparency (mirrors the Trust Score approach)
    private MatchBreakdown breakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchBreakdown {
        private double skillCompatibility;   // out of 40
        private double reciprocalPotential;  // out of 20
        private double experienceFit;        // out of 15
        private double availabilityOverlap;  // out of 10
        private double reputation;           // out of 10
        private double activity;             // out of 5
    }
}
