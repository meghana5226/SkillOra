package com.skillora.service;

import com.skillora.entity.User;
import com.skillora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Centralizes the platform's XP economy so point awards and level-ups happen
 * consistently everywhere they're triggered (sessions, reviews, profile completion).
 *
 * Levels:
 *   1: 0-99   2: 100-249   3: 250-499   4: 500-999   5: 1000+
 */
@Service
@RequiredArgsConstructor
public class GamificationService {

    public static final int XP_SESSION_COMPLETE = 50;
    public static final int XP_FIVE_STAR_REVIEW = 25;
    public static final int XP_PROFILE_COMPLETE = 20;

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public void awardPoints(User user, int amount, String reason) {
        int newTotal = user.getPoints() + amount;
        user.setPoints(newTotal);

        int oldLevel = user.getLevel();
        int newLevel = computeLevel(newTotal);
        user.setLevel(newLevel);
        userRepository.save(user);

        if (newLevel > oldLevel) {
            notificationService.notify(user, "ACHIEVEMENT",
                    "Level up! You're now Level " + newLevel,
                    "You earned " + amount + " XP (" + reason + ") and reached Level " + newLevel + ".");
        } else {
            notificationService.notify(user, "XP",
                    "+" + amount + " XP",
                    reason);
        }
    }

    public static int computeLevel(int points) {
        if (points >= 1000) return 5;
        if (points >= 500) return 4;
        if (points >= 250) return 3;
        if (points >= 100) return 2;
        return 1;
    }
}
