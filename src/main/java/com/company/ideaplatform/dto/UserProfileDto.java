package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class UserProfileDto {
    private Long id;
    private String displayName;
    private String email;
    private String teamName;
    private String tribeName;
    private String divisionName;
    private long totalIdeas;
    private long implementedIdeas;
    private long votesGiven;
    private Map<String, Long> achievementsByHero;
    private List<UserAchievementDto> recentAchievements;
    private List<IdeaViewDto> recentIdeas;
}
