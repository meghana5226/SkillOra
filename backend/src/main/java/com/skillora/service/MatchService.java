package com.skillora.service;

import com.skillora.dto.match.MatchResponse;
import com.skillora.entity.*;
import com.skillora.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes a transparent, explainable "reciprocal match score" between the current
 * user and every other active user on the platform. No external AI call is used -
 * the score is a weighted sum of concrete signals, all derived from data already
 * stored in Postgres:
 *
 *   40% skill compatibility      - how much of what each side wants, the other can teach
 *   20% reciprocal potential     - bonus for a genuine two-way exchange vs. one-directional
 *   15% experience compatibility - whether the teacher's proficiency meets the learner's target level
 *   10% availability overlap     - shared tokens between the two users' free-text availability
 *   10% reputation               - the candidate's average review rating
 *    5% activity                 - a light proxy on accumulated platform activity (XP)
 *
 * This is intentionally simple and inspectable (see MatchResponse.MatchBreakdown) rather
 * than a black box, per the "Trust Score" transparency principle used elsewhere in the app.
 */
@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserRepository userRepository;
    private final UserOfferedSkillRepository offeredSkillRepository;
    private final UserWantedSkillRepository wantedSkillRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<MatchResponse> findMatches(Long currentUserId, int limit) {
        User me = userRepository.findById(currentUserId).orElseThrow();

        List<UserOfferedSkill> myOffered = offeredSkillRepository.findByUserId(currentUserId);
        List<UserWantedSkill> myWanted = wantedSkillRepository.findByUserId(currentUserId);

        Map<Long, UserOfferedSkill> myOfferedBySkill = myOffered.stream()
                .collect(Collectors.toMap(o -> o.getSkill().getId(), o -> o, (a, b) -> a));
        Map<Long, UserWantedSkill> myWantedBySkill = myWanted.stream()
                .collect(Collectors.toMap(w -> w.getSkill().getId(), w -> w, (a, b) -> a));

        List<User> candidates = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .filter(u -> Boolean.TRUE.equals(u.getActive()))
                .toList();

        List<MatchResponse> results = new ArrayList<>();

        for (User candidate : candidates) {
            List<UserOfferedSkill> theirOffered = offeredSkillRepository.findByUserId(candidate.getId());
            List<UserWantedSkill> theirWanted = wantedSkillRepository.findByUserId(candidate.getId());

            if (theirOffered.isEmpty() && theirWanted.isEmpty() && myOffered.isEmpty() && myWanted.isEmpty()) {
                continue; // nothing to match on for either side yet
            }

            Map<Long, UserOfferedSkill> theirOfferedBySkill = theirOffered.stream()
                    .collect(Collectors.toMap(o -> o.getSkill().getId(), o -> o, (a, b) -> a));
            Map<Long, UserWantedSkill> theirWantedBySkill = theirWanted.stream()
                    .collect(Collectors.toMap(w -> w.getSkill().getId(), w -> w, (a, b) -> a));

            // Skills the candidate can teach ME (their offered ∩ my wanted)
            List<UserOfferedSkill> theyCanTeachYou = theirOffered.stream()
                    .filter(o -> myWantedBySkill.containsKey(o.getSkill().getId()))
                    .toList();

            // Skills I can teach THEM (my offered ∩ their wanted)
            List<UserOfferedSkill> youCanTeachThem = myOffered.stream()
                    .filter(o -> theirWantedBySkill.containsKey(o.getSkill().getId()))
                    .toList();

            double skillCompatibility = scoreSkillCompatibility(theyCanTeachYou, youCanTeachThem, myWanted, theirWanted);
            double reciprocal = scoreReciprocal(theyCanTeachYou, youCanTeachThem);
            double experienceFit = scoreExperienceFit(theyCanTeachYou, myWantedBySkill);
            double availability = scoreAvailability(me.getAvailability(), candidate.getAvailability());
            double reputation = scoreReputation(candidate.getId());
            double activity = scoreActivity(candidate.getPoints());

            double total = skillCompatibility + reciprocal + experienceFit + availability + reputation + activity;
            int percentage = (int) Math.round(Math.min(100, Math.max(0, total)));

            if (theyCanTeachYou.isEmpty() && youCanTeachThem.isEmpty()) {
                continue; // no basis for a match at all - don't show noise
            }

            MatchResponse match = MatchResponse.builder()
                    .userId(candidate.getId())
                    .name(candidate.getName())
                    .profileImage(candidate.getProfileImage())
                    .location(candidate.getLocation())
                    .averageRating(reviewRepository.averageRatingForUser(candidate.getId()))
                    .matchPercentage(percentage)
                    .theyCanTeachYou(theyCanTeachYou.stream().map(o -> o.getSkill().getName()).toList())
                    .youCanTeachThem(youCanTeachThem.stream().map(o -> o.getSkill().getName()).toList())
                    .explanation(buildExplanation(youCanTeachThem, theyCanTeachYou))
                    .breakdown(MatchResponse.MatchBreakdown.builder()
                            .skillCompatibility(round1(skillCompatibility))
                            .reciprocalPotential(round1(reciprocal))
                            .experienceFit(round1(experienceFit))
                            .availabilityOverlap(round1(availability))
                            .reputation(round1(reputation))
                            .activity(round1(activity))
                            .build())
                    .build();

            results.add(match);
        }

        results.sort(Comparator.comparingInt(MatchResponse::getMatchPercentage).reversed());
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    private double scoreSkillCompatibility(List<UserOfferedSkill> theyCanTeachYou, List<UserOfferedSkill> youCanTeachThem,
                                            List<UserWantedSkill> myWanted, List<UserWantedSkill> theirWanted) {
        double coverageOfMyWants = myWanted.isEmpty() ? 0 : (double) theyCanTeachYou.size() / myWanted.size();
        double coverageOfTheirWants = theirWanted.isEmpty() ? 0 : (double) youCanTeachThem.size() / theirWanted.size();
        double avg = (coverageOfMyWants + coverageOfTheirWants) / 2.0;
        return 40.0 * Math.min(1.0, avg);
    }

    private double scoreReciprocal(List<UserOfferedSkill> theyCanTeachYou, List<UserOfferedSkill> youCanTeachThem) {
        boolean forward = !youCanTeachThem.isEmpty();
        boolean backward = !theyCanTeachYou.isEmpty();
        if (forward && backward) return 20.0;
        if (forward || backward) return 8.0;
        return 0.0;
    }

    private double scoreExperienceFit(List<UserOfferedSkill> theyCanTeachYou, Map<Long, UserWantedSkill> myWantedBySkill) {
        if (theyCanTeachYou.isEmpty()) return 7.5; // neutral - no basis to judge yet
        long satisfied = theyCanTeachYou.stream()
                .filter(o -> {
                    UserWantedSkill want = myWantedBySkill.get(o.getSkill().getId());
                    return want != null && o.getProficiencyLevel().ordinal() >= want.getTargetLevel().ordinal();
                })
                .count();
        return 15.0 * ((double) satisfied / theyCanTeachYou.size());
    }

    private double scoreAvailability(String mine, String theirs) {
        if (mine == null || theirs == null || mine.isBlank() || theirs.isBlank()) return 0.0;
        Set<String> mineTokens = tokenize(mine);
        Set<String> theirTokens = tokenize(theirs);
        if (mineTokens.isEmpty() || theirTokens.isEmpty()) return 0.0;
        Set<String> common = new HashSet<>(mineTokens);
        common.retainAll(theirTokens);
        double fraction = (double) common.size() / Math.min(mineTokens.size(), theirTokens.size());
        return 10.0 * Math.min(1.0, fraction);
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[^a-z0-9]+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private double scoreReputation(Long candidateId) {
        Double avg = reviewRepository.averageRatingForUser(candidateId);
        if (avg == null) return 5.0; // neutral for users with no reviews yet
        return 10.0 * Math.min(1.0, avg / 5.0);
    }

    private double scoreActivity(Integer points) {
        if (points == null || points <= 0) return 0.0;
        return 5.0 * Math.min(1.0, points / 200.0);
    }

    private String buildExplanation(List<UserOfferedSkill> youCanTeachThem, List<UserOfferedSkill> theyCanTeachYou) {
        String teach = youCanTeachThem.isEmpty() ? null : youCanTeachThem.get(0).getSkill().getName();
        String learn = theyCanTeachYou.isEmpty() ? null : theyCanTeachYou.get(0).getSkill().getName();

        if (teach != null && learn != null) {
            return "You can teach " + teach + " while learning " + learn + ".";
        } else if (learn != null) {
            return "They can teach you " + learn + ".";
        } else if (teach != null) {
            return "You can teach them " + teach + ".";
        }
        return "Potential match based on shared skill interests.";
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
