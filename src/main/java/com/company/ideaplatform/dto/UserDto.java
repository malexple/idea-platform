package com.company.ideaplatform.dto;

import com.company.ideaplatform.entity.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String displayName;
    private UserRole role;
    private String teamName;
    private String tribeName;
    private String divisionName;
    private long ideasCount;
    private long implementedCount;
    private long votesGiven;
    private long achievementsCount;
}
