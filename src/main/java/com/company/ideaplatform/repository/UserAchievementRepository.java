package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.UserAchievement;
import com.company.ideaplatform.entity.enums.HeroType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(Long userId);

    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);

    boolean existsByUserIdAndAchievementCode(Long userId, String achievementCode);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua " +
            "WHERE ua.user.id = :userId AND ua.achievement.heroType = :heroType")
    long countByUserIdAndHeroType(@Param("userId") Long userId, @Param("heroType") HeroType heroType);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId " +
            "ORDER BY ua.earnedAt DESC LIMIT 5")
    List<UserAchievement> findRecentByUserId(@Param("userId") Long userId);
}
