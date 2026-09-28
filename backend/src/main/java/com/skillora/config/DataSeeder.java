package com.skillora.config;

import com.skillora.entity.*;
import com.skillora.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Seeds realistic demo data on startup so the app is immediately usable after
 * `docker compose up --build`, without requiring any manual data entry.
 *
 * This only runs when app.seed.enabled=true (default) AND the users table is empty,
 * so it never duplicates data on subsequent restarts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final UserOfferedSkillRepository offeredSkillRepository;
    private final UserWantedSkillRepository wantedSkillRepository;
    private final SwapRequestRepository swapRequestRepository;
    private final SessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final LearningGoalRepository learningGoalRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.demo-password:Demo@1234}")
    private String demoPassword;

    private final Random random = new Random(42);

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Seeding disabled via app.seed.enabled=false");
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Database already has data - skipping seed");
            return;
        }

        log.info("Seeding Skillora demo data...");

        List<Skill> skills = seedSkills();
        List<User> users = seedUsers(skills);
        seedSwapsAndSessionsAndReviews(users, skills);
        seedNotificationsAndGoals(users, skills);

        log.info("Seed complete: {} users, {} skills", users.size(), skills.size());
    }

    private List<Skill> seedSkills() {
        String[][] data = {
            {"Java", "Programming"}, {"Spring Boot", "Programming"}, {"Python", "Programming"},
            {"JavaScript", "Programming"}, {"TypeScript", "Programming"}, {"React", "Programming"},
            {"Node.js", "Programming"}, {"SQL", "Programming"}, {"Git", "Programming"},
            {"Docker", "Programming"}, {"Data Analysis", "Data Science"}, {"Machine Learning", "AI & ML"},
            {"Deep Learning", "AI & ML"}, {"Prompt Engineering", "AI & ML"}, {"UI Design", "Design"},
            {"UX Research", "Design"}, {"Figma", "Design"}, {"Adobe Photoshop", "Design"},
            {"Digital Marketing", "Marketing"}, {"SEO", "Marketing"}, {"Copywriting", "Marketing"},
            {"Public Speaking", "Communication"}, {"Technical Writing", "Communication"},
            {"Negotiation", "Communication"}, {"Spanish", "Languages"}, {"French", "Languages"},
            {"Japanese", "Languages"}, {"Guitar", "Music"}, {"Piano", "Music"}, {"Music Theory", "Music"},
            {"Personal Finance", "Finance"}, {"Investing Basics", "Finance"}, {"Resume Writing", "Career"},
            {"Interview Prep", "Career"}, {"Product Management", "Career"}
        };
        List<Skill> skills = new ArrayList<>();
        for (String[] d : data) {
            skills.add(skillRepository.save(Skill.builder()
                    .name(d[0]).category(d[1])
                    .description(d[0] + " - " + d[1] + " skill on Skillora")
                    .build()));
        }
        return skills;
    }

    private Skill byName(List<Skill> skills, String name) {
        return skills.stream().filter(s -> s.getName().equals(name)).findFirst().orElseThrow();
    }

    private List<User> seedUsers(List<Skill> skills) {
        List<User> users = new ArrayList<>();

        // Fixed demo accounts called out in the README
        User admin = buildUser("Skillora Admin", "admin@skillora.demo", Role.ADMIN, "San Francisco, USA", 0);
        admin = userRepository.save(admin);
        users.add(admin);

        User alex = buildUser("Alex Morgan", "alex@skillora.demo", Role.USER, "Austin, USA", 180);
        alex = userRepository.save(alex);
        users.add(alex);
        offer(alex, byName(skills, "Java"), ProficiencyLevel.MENTOR, 6);
        offer(alex, byName(skills, "SQL"), ProficiencyLevel.ADVANCED, 5);
        offer(alex, byName(skills, "Git"), ProficiencyLevel.ADVANCED, 5);
        want(alex, byName(skills, "React"), ProficiencyLevel.INTERMEDIATE, 1);
        want(alex, byName(skills, "UI Design"), ProficiencyLevel.BEGINNER, 2);

        User sarah = buildUser("Sarah Chen", "sarah@skillora.demo", Role.USER, "Seattle, USA", 260);
        sarah = userRepository.save(sarah);
        users.add(sarah);
        offer(sarah, byName(skills, "React"), ProficiencyLevel.MENTOR, 4);
        offer(sarah, byName(skills, "UI Design"), ProficiencyLevel.ADVANCED, 3);
        offer(sarah, byName(skills, "Figma"), ProficiencyLevel.ADVANCED, 3);
        want(sarah, byName(skills, "Java"), ProficiencyLevel.INTERMEDIATE, 1);
        want(sarah, byName(skills, "Spring Boot"), ProficiencyLevel.BEGINNER, 2);

        String[] firstNames = {"Maya", "Liam", "Priya", "Noah", "Emma", "Ravi", "Olivia", "Ethan",
                "Sofia", "Mason", "Ava", "Lucas", "Zara", "Owen", "Mia", "Diego", "Chloe", "Kenji"};
        String[] lastNames = {"Patel", "Garcia", "Kim", "Nguyen", "Smith", "Rossi", "Müller", "Silva",
                "Johansson", "O'Brien", "Tanaka", "Costa", "Ivanov", "Dubois", "Hassan", "Park", "Lopez", "Novak"};
        String[] cities = {"Toronto, Canada", "London, UK", "Berlin, Germany", "Mumbai, India",
                "Sydney, Australia", "Singapore", "Dublin, Ireland", "Barcelona, Spain",
                "Tokyo, Japan", "Sao Paulo, Brazil", "Hyderabad, India", "Amsterdam, Netherlands"};
        String[] availabilityOptions = {
                "Saturday morning, Sunday afternoon", "Weekday evenings after 6pm",
                "Sunday morning, weekday evenings", "Saturday afternoon, Sunday morning",
                "Weekday mornings before 9am", "Flexible weekends"
        };

        List<String> skillPool = List.of("Python", "JavaScript", "TypeScript", "Node.js", "Docker",
                "Data Analysis", "Machine Learning", "UX Research", "Adobe Photoshop", "Digital Marketing",
                "SEO", "Copywriting", "Public Speaking", "Technical Writing", "Spanish", "French",
                "Guitar", "Piano", "Personal Finance", "Resume Writing", "Interview Prep", "Product Management");

        for (int i = 0; i < 18; i++) {
            String name = firstNames[i] + " " + lastNames[i];
            String email = (firstNames[i] + "." + lastNames[i]).toLowerCase().replace("'", "") + "@skillora.demo";
            User u = buildUser(name, email, Role.USER, cities[i % cities.length], random.nextInt(300));
            u.setAvailability(availabilityOptions[i % availabilityOptions.length]);
            u.setExperienceLevel(ProficiencyLevel.values()[random.nextInt(3)]);
            u = userRepository.save(u);
            users.add(u);

            // each demo user offers 1-2 skills and wants 1-2 skills, so matches exist across the pool
            String teachSkill = skillPool.get(random.nextInt(skillPool.size()));
            String learnSkill = skillPool.get(random.nextInt(skillPool.size()));
            offer(u, byName(skills, teachSkill), ProficiencyLevel.values()[1 + random.nextInt(3)], 1 + random.nextInt(8));
            if (!learnSkill.equals(teachSkill)) {
                want(u, byName(skills, learnSkill), ProficiencyLevel.values()[random.nextInt(3)], 1 + random.nextInt(3));
            }
            // also give reciprocal chances with Alex/Sarah's skill sets
            if (i % 3 == 0) want(u, byName(skills, "Java"), ProficiencyLevel.BEGINNER, 1);
            if (i % 4 == 0) offer(u, byName(skills, "Git"), ProficiencyLevel.INTERMEDIATE, 2);
        }

        return users;
    }

    private User buildUser(String name, String email, Role role, String location, int points) {
        return User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(demoPassword))
                .role(role)
                .location(location)
                .bio("Skillora member based in " + location + ".")
                .timezone("UTC")
                .availability("Weekends")
                .experienceLevel(ProficiencyLevel.INTERMEDIATE)
                .points(points)
                .level(com.skillora.service.GamificationService.computeLevel(points))
                .reputationScore(BigDecimal.ZERO)
                .onboardingComplete(true)
                .active(true)
                .build();
    }

    private void offer(User user, Skill skill, ProficiencyLevel level, int years) {
        if (offeredSkillRepository.existsByUserIdAndSkillId(user.getId(), skill.getId())) return;
        offeredSkillRepository.save(UserOfferedSkill.builder()
                .user(user).skill(skill).proficiencyLevel(level).yearsExperience(years).build());
    }

    private void want(User user, Skill skill, ProficiencyLevel target, int priority) {
        if (wantedSkillRepository.existsByUserIdAndSkillId(user.getId(), skill.getId())) return;
        wantedSkillRepository.save(UserWantedSkill.builder()
                .user(user).skill(skill).targetLevel(target).priority(priority).build());
    }

    private void seedSwapsAndSessionsAndReviews(List<User> users, List<Skill> skills) {
        User alex = users.get(1);
        User sarah = users.get(2);

        SwapRequest swap = swapRequestRepository.save(SwapRequest.builder()
                .sender(alex).receiver(sarah)
                .offeredSkill(byName(skills, "Java"))
                .requestedSkill(byName(skills, "React"))
                .message("Happy to walk through Spring Boot basics if you can help me get comfortable with React hooks!")
                .status(SwapStatus.ACCEPTED)
                .build());

        Session completedSession = sessionRepository.save(Session.builder()
                .requester(alex).receiver(sarah)
                .skill(byName(skills, "React"))
                .scheduledDate(LocalDate.now().minusDays(5))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(12, 0))
                .meetingLink("https://meet.example.com/demo-session-1")
                .status(SessionStatus.COMPLETED)
                .notes("Covered useState/useEffect and component structure.")
                .build());

        sessionRepository.save(Session.builder()
                .requester(sarah).receiver(alex)
                .skill(byName(skills, "Java"))
                .scheduledDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(19, 0))
                .meetingLink("https://meet.example.com/demo-session-2")
                .status(SessionStatus.CONFIRMED)
                .notes("Intro to Spring Boot project structure.")
                .build());

        reviewRepository.save(Review.builder()
                .reviewer(alex).reviewedUser(sarah).session(completedSession)
                .rating(5).comment("Sarah is a fantastic teacher - explained hooks really clearly!")
                .build());
        sarah.setReputationScore(BigDecimal.valueOf(5.0));
        sarah.setPoints(sarah.getPoints() + 50 + 25);
        sarah.setLevel(com.skillora.service.GamificationService.computeLevel(sarah.getPoints()));
        userRepository.save(sarah);

        alex.setPoints(alex.getPoints() + 50);
        alex.setLevel(com.skillora.service.GamificationService.computeLevel(alex.getPoints()));
        userRepository.save(alex);

        // a couple more pending swap requests among the wider pool, for a realistic dashboard
        for (int i = 3; i < Math.min(users.size(), 8); i++) {
            User sender = users.get(i);
            User receiver = users.get((i + 2) % users.size() == 0 ? 1 : (i + 2) % users.size());
            if (sender.getId().equals(receiver.getId())) continue;
            List<UserOfferedSkill> senderOffers = offeredSkillRepository.findByUserId(sender.getId());
            List<UserWantedSkill> senderWants = wantedSkillRepository.findByUserId(sender.getId());
            if (senderOffers.isEmpty() || senderWants.isEmpty()) continue;
            swapRequestRepository.save(SwapRequest.builder()
                    .sender(sender).receiver(receiver)
                    .offeredSkill(senderOffers.get(0).getSkill())
                    .requestedSkill(senderWants.get(0).getSkill())
                    .message("Would love to trade skills - let me know if you're interested!")
                    .status(SwapStatus.PENDING)
                    .build());
        }
    }

    private void seedNotificationsAndGoals(List<User> users, List<Skill> skills) {
        User alex = users.get(1);
        User sarah = users.get(2);

        notificationRepository.save(Notification.builder()
                .user(alex).type("ACHIEVEMENT").title("Welcome to Skillora!")
                .message("Complete your profile and add your first skill to start matching.")
                .read(false).build());

        notificationRepository.save(Notification.builder()
                .user(sarah).type("REVIEW_RECEIVED").title("You received a 5-star review")
                .message("Alex Morgan left you a review.")
                .read(false).build());

        learningGoalRepository.save(LearningGoal.builder()
                .user(alex).title("Become interview-ready in React")
                .skill(byName(skills, "React"))
                .targetDate(LocalDate.now().plusMonths(2))
                .progress(35)
                .status(GoalStatus.IN_PROGRESS)
                .build());

        learningGoalRepository.save(LearningGoal.builder()
                .user(sarah).title("Get comfortable building REST APIs with Spring Boot")
                .skill(byName(skills, "Spring Boot"))
                .targetDate(LocalDate.now().plusMonths(3))
                .progress(15)
                .status(GoalStatus.IN_PROGRESS)
                .build());
    }
}
