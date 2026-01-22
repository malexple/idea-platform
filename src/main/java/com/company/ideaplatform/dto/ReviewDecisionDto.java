package com.company.ideaplatform.dto;

import com.company.ideaplatform.entity.enums.IdeaStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDecisionDto {

    @NotNull(message = "Решение обязательно")
    private IdeaStatus decision;

    private String comment;

    private Long duplicateOfIdeaId;
}
