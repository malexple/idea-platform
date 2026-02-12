package com.company.ideaplatform.dto;

import com.company.ideaplatform.entity.enums.AchievementLevel;
import com.company.ideaplatform.entity.enums.HeroType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserAchievementDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private HeroType heroType;
    private AchievementLevel level;
    private String iconPath;
    private LocalDateTime earnedAt;
    private String relatedIdeaNumber;
}
