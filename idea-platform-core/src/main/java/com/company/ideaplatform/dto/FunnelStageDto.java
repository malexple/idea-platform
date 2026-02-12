package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FunnelStageDto {
    private String status;
    private String displayName;
    private long count;
    private double percentage;
    private double avgDaysInStage;
}
