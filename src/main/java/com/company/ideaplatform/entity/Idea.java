package com.company.ideaplatform.entity;

import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.entity.enums.IdeaType;
import com.company.ideaplatform.entity.enums.Priority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ideas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Idea extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdeaType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "expected_effect", columnDefinition = "TEXT", nullable = false)
    private String expectedEffect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IdeaStatus status = IdeaStatus.DRAFT;

    @Builder.Default
    @Column(nullable = false)
    private Boolean anonymous = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tribe_id", nullable = false)
    private Tribe tribe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_idea_id")
    private Idea parentIdea;

    @OneToMany(mappedBy = "parentIdea")
    @Builder.Default
    private List<Idea> duplicates = new ArrayList<>();

    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;

    @Column(name = "jira_link")
    private String jiraLink;

    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ReviewAssignment> reviewAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL)
    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();
}
