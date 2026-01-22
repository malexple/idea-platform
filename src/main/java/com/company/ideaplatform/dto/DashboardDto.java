package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardDto {
    private Map<String, Long> ideasByType;
    private Map<String, Long> ideasByStatus;
    private Map<String, Long> ideasByTeam;
    private List<IdeaViewDto> topVotedIdeas;
    private List<LeaderboardEntryDto> topAuthors;
    private double avgTimeToReviewDays;
    private double avgTimeToImplementDays;
    private long totalIdeas;
    private long totalImplemented;
    private long totalUsers;
}
