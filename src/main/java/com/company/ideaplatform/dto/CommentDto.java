package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;
    private String authorName;
    private Long authorId;
    private String text;
    private LocalDateTime createdAt;
    private boolean edited;
}
