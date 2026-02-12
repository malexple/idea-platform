package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ManagerDashboardDto {
    // Общие метрики
    private long totalUsers;
    private long activeUsers;
    private long totalIdeas;
    private long implementedIdeas;

    // Распределение по категориям
    private long innovatorsCount;        // 1-2%
    private long earlyAdoptersCount;     // 13-15%
    private long participantsCount;
    private long observersCount;
    private long inactiveCount;

    // Воронка
    private List<FunnelStageDto> funnel;

    // Конверсии
    private double submittedToImplementedRate;
    private double avgDaysToImplement;

    // Топы
    private List<InnovatorDto> topInnovators;
    private List<TeamActivityDto> topTeams;
}
