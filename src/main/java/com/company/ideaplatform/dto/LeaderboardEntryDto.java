package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntryDto {
    private Long userId;
    private String displayName;
    private String teamName;
    private long ideasCount;
    private long implementedCount;
    private long totalVotesReceived;
    private long achievementsCount;
}
