package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InnovatorDto {
    private Long userId;
    private String displayName;
    private String email;
    private String teamName;
    private String category;  // INNOVATOR, EARLY_ADOPTER, PARTICIPANT, OBSERVER, INACTIVE
    private long ideasCount;
    private long implementedCount;
    private long votesCount;
    private long commentsCount;
    private double activityScore;
}
