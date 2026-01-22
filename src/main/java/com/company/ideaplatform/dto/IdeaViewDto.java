package com.company.ideaplatform.dto;

import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.entity.enums.IdeaType;
import com.company.ideaplatform.entity.enums.Priority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class IdeaViewDto {
    private Long id;
    private String number;
    private IdeaType type;
    private String title;
    private String description;
    private String expectedEffect;
    private Priority priority;
    private IdeaStatus status;
    private boolean anonymous;
    private String authorName;
    private Long authorId;
    private String divisionName;
    private String tribeName;
    private String teamName;
    private LocalDateTime createdAt;
    private LocalDateTime reviewDeadline;
    private String jiraLink;
    private String parentIdeaNumber;
    private Long parentIdeaId;
    private List<AttachmentDto> attachments;
    private List<CommentDto> comments;
    private Map<String, Long> votesCounts;
    private long totalVotes;
    private String currentUserVote;
}
