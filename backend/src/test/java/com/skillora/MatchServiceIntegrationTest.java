package com.skillora;

import com.skillora.dto.match.MatchResponse;
import com.skillora.entity.*;
import com.skillora.repository.*;
import com.skillora.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MatchServiceIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private UserOfferedSkillRepository offeredSkillRepository;
    @Autowired private UserWantedSkillRepository wantedSkillRepository;
    @Autowired private MatchService matchService;

    @Test
    void reciprocalMatch_scoresHigherThanOneDirectionalMatch() {
        Skill java = skillRepository.save(Skill.builder().name("Java-T1").category("Programming").build());
        Skill spring = skillRepository.save(Skill.builder().name("SpringBoot-T1").category("Programming").build());
        Skill sql = skillRepository.save(Skill.builder().name("SQL-T1").category("Programming").build());

        User userA = userRepository.save(baseUser("a-" + System.nanoTime() + "@test.com"));
        User userB = userRepository.save(baseUser("b-" + System.nanoTime() + "@test.com")); // reciprocal
        User userC = userRepository.save(baseUser("c-" + System.nanoTime() + "@test.com")); // one-directional only

        // User A wants Java + Spring Boot, offers SQL
        wantedSkillRepository.save(UserWantedSkill.builder().user(userA).skill(java).targetLevel(ProficiencyLevel.INTERMEDIATE).priority(1).build());
        wantedSkillRepository.save(UserWantedSkill.builder().user(userA).skill(spring).targetLevel(ProficiencyLevel.INTERMEDIATE).priority(1).build());
        offeredSkillRepository.save(UserOfferedSkill.builder().user(userA).skill(sql).proficiencyLevel(ProficiencyLevel.ADVANCED).yearsExperience(3).build());

        // User B: offers Java + Spring Boot, wants SQL -> full reciprocal match with A
        offeredSkillRepository.save(UserOfferedSkill.builder().user(userB).skill(java).proficiencyLevel(ProficiencyLevel.MENTOR).yearsExperience(6).build());
        offeredSkillRepository.save(UserOfferedSkill.builder().user(userB).skill(spring).proficiencyLevel(ProficiencyLevel.ADVANCED).yearsExperience(4).build());
        wantedSkillRepository.save(UserWantedSkill.builder().user(userB).skill(sql).targetLevel(ProficiencyLevel.BEGINNER).priority(1).build());

        // User C: offers Java only, wants nothing A offers -> one-directional match only
        offeredSkillRepository.save(UserOfferedSkill.builder().user(userC).skill(java).proficiencyLevel(ProficiencyLevel.MENTOR).yearsExperience(6).build());

        List<MatchResponse> matches = matchService.findMatches(userA.getId(), 10);

        MatchResponse matchWithB = matches.stream().filter(m -> m.getUserId().equals(userB.getId())).findFirst().orElseThrow();
        MatchResponse matchWithC = matches.stream().filter(m -> m.getUserId().equals(userC.getId())).findFirst().orElseThrow();

        assertThat(matchWithB.getMatchPercentage()).isGreaterThan(matchWithC.getMatchPercentage());
        assertThat(matchWithB.getTheyCanTeachYou()).contains("Java-T1", "SpringBoot-T1");
        assertThat(matchWithB.getYouCanTeachThem()).contains("SQL-T1");
        assertThat(matchWithB.getBreakdown().getReciprocalPotential()).isEqualTo(20.0);
        assertThat(matchWithC.getBreakdown().getReciprocalPotential()).isEqualTo(8.0);
    }

    private User baseUser(String email) {
        return User.builder()
                .name("Match Test " + email)
                .email(email)
                .password("hashed")
                .role(Role.USER)
                .experienceLevel(ProficiencyLevel.INTERMEDIATE)
                .points(0)
                .level(1)
                .reputationScore(BigDecimal.ZERO)
                .onboardingComplete(true)
                .active(true)
                .build();
    }
}
