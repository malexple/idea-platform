package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.*;
import com.company.ideaplatform.entity.enums.*;
import com.company.ideaplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final IdeaRepository ideaRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;

    // Thresholds for achievements
    private static final Map<String, Integer> THRESHOLDS = Map.ofEntries(
            // Owl - Problems
            Map.entry("OWL_NOVICE", 1),
            Map.entry("OWL_EXPLORER", 5),
            Map.entry("OWL_MASTER", 15),
            Map.entry("OWL_EXPERT", 30),
            Map.entry("OWL_LEGEND", 50),
            // Bee - Automation
            Map.entry("BEE_NOVICE", 1),
            Map.entry("BEE_EXPLORER", 5),
            Map.entry("BEE_MASTER", 15),
            Map.entry("BEE_EXPERT", 30),
            Map.entry("BEE_LEGEND", 50),
            // Eagle - Ideas
            Map.entry("EAGLE_NOVICE", 1),
            Map.entry("EAGLE_EXPLORER", 5),
            Map.entry("EAGLE_MASTER", 15),
            Map.entry("EAGLE_EXPERT", 30),
            Map.entry("EAGLE_LEGEND", 50),
            // Phoenix - Implementation
            Map.entry("PHOENIX_NOVICE", 1),
            Map.entry("PHOENIX_EXPLORER", 3),
            Map.entry("PHOENIX_MASTER", 5),
            Map.entry("PHOENIX_EXPERT", 10),
            Map.entry("PHOENIX_LEGEND", 20),
            // Dolphin - Community
            Map.entry("DOLPHIN_VOTE_NOVICE", 1),
            Map.entry("DOLPHIN_VOTE_EXPLORER", 20),
            Map.entry("DOLPHIN_VOTE_MASTER", 50),
            Map.entry("DOLPHIN_VOTE_EXPERT", 100),
            Map.entry("DOLPHIN_VOTE_LEGEND", 200),
            Map.entry("DOLPHIN_COMMENT_EXPLORER", 10),
            Map.entry("DOLPHIN_COMMENT_MASTER", 30),
            Map.entry("DOLPHIN_COMMENT_EXPERT", 50)
    );

    @Transactional
    public void checkAndAwardAchievements(User user, Idea idea) {
        IdeaType type = idea.getType();

        switch (type) {
            case PROBLEM -> checkProblemAchievements(user, idea);
            case AUTOMATION -> checkAutomationAchievements(user, idea);
            case IDEA -> checkIdeaAchievements(user, idea);
        }
    }

    @Transactional
    public void checkProblemAchievements(User user, Idea idea) {
        long count = ideaRepository.countByAuthorIdAndType(user.getId(), IdeaType.PROBLEM);

        checkAndAward(user, "OWL_NOVICE", count, idea);
        checkAndAward(user, "OWL_EXPLORER", count, idea);
        checkAndAward(user, "OWL_MASTER", count, idea);
        checkAndAward(user, "OWL_EXPERT", count, idea);
        checkAndAward(user, "OWL_LEGEND", count, idea);
    }

    @Transactional
    public void checkAutomationAchievements(User user, Idea idea) {
        long count = ideaRepository.countByAuthorIdAndType(user.getId(), IdeaType.AUTOMATION);

        checkAndAward(user, "BEE_NOVICE", count, idea);
        checkAndAward(user, "BEE_EXPLORER", count, idea);
        checkAndAward(user, "BEE_MASTER", count, idea);
        checkAndAward(user, "BEE_EXPERT", count, idea);
        checkAndAward(user, "BEE_LEGEND", count, idea);
    }

    @Transactional
    public void checkIdeaAchievements(User user, Idea idea) {
        long count = ideaRepository.countByAuthorIdAndType(user.getId(), IdeaType.IDEA);

        checkAndAward(user, "EAGLE_NOVICE", count, idea);
        checkAndAward(user, "EAGLE_EXPLORER", count, idea);
        checkAndAward(user, "EAGLE_MASTER", count, idea);
        checkAndAward(user, "EAGLE_EXPERT", count, idea);
        checkAndAward(user, "EAGLE_LEGEND", count, idea);
    }

    @Transactional
    public void checkImplementationAchievements(User user, Idea idea) {
        long count = ideaRepository.countByAuthorIdAndStatus(user.getId(), IdeaStatus.IMPLEMENTED);

        checkAndAward(user, "PHOENIX_NOVICE", count, idea);
        checkAndAward(user, "PHOENIX_EXPLORER", count, idea);
        checkAndAward(user, "PHOENIX_MASTER", count, idea);
        checkAndAward(user, "PHOENIX_EXPERT", count, idea);
        checkAndAward(user, "PHOENIX_LEGEND", count, idea);
    }

    @Transactional
    public void checkVotingAchievements(User user) {
        long count = voteRepository.countByUserId(user.getId());

        checkAndAward(user, "DOLPHIN_VOTE_NOVICE", count, null);
        checkAndAward(user, "DOLPHIN_VOTE_EXPLORER", count, null);
        checkAndAward(user, "DOLPHIN_VOTE_MASTER", count, null);
        checkAndAward(user, "DOLPHIN_VOTE_EXPERT", count, null);
        checkAndAward(user, "DOLPHIN_VOTE_LEGEND", count, null);
    }

    @Transactional
    public void checkCommentingAchievements(User user) {
        long count = commentRepository.countByAuthorId(user.getId());

        checkAndAward(user, "DOLPHIN_COMMENT_EXPLORER", count, null);
        checkAndAward(user, "DOLPHIN_COMMENT_MASTER", count, null);
        checkAndAward(user, "DOLPHIN_COMMENT_EXPERT", count, null);
    }

    private void checkAndAward(User user, String achievementCode, long currentCount, Idea relatedIdea) {
        Integer threshold = THRESHOLDS.get(achievementCode);
        if (threshold == null || currentCount < threshold) {
            return;
        }

        if (userAchievementRepository.existsByUserIdAndAchievementCode(user.getId(), achievementCode)) {
            return;
        }

        achievementRepository.findByCode(achievementCode).ifPresent(achievement -> {
            UserAchievement userAchievement = UserAchievement.builder()
                    .user(user)
                    .achievement(achievement)
                    .earnedAt(LocalDateTime.now())
                    .relatedIdea(relatedIdea)
                    .build();

            userAchievementRepository.save(userAchievement);
            log.info("User {} earned achievement: {}", user.getEmail(), achievementCode);
        });
    }

    public List<Achievement> getAllAchievements() {
        return achievementRepository.findByActiveTrueOrderBySortOrder();
    }

    public List<Achievement> getAchievementsByHero(HeroType heroType) {
        return achievementRepository.findByHeroTypeAndActiveTrue(heroType);
    }
}
