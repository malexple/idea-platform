package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamActivityDto {
    private Long teamId;
    private String teamName;
    private String tribeName;
    private String divisionName;
    private long membersCount;
    private long ideasCount;
    private long implementedCount;
    private long activeUsersCount;
    private double engagementRate;  // activeUsers / membersCount * 100
}
