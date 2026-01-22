package com.company.ideaplatform.repository;

import com.company.ideaplatform.entity.Achievement;
import com.company.ideaplatform.entity.enums.HeroType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByCode(String code);
    List<Achievement> findByHeroTypeAndActiveTrue(HeroType heroType);
    List<Achievement> findByActiveTrueOrderBySortOrder();
}
