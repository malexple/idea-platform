package com.company.ideaplatform.entity;

import com.company.ideaplatform.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "auth_provider", nullable = false)
    @Builder.Default
    private String authProvider = "local";

    @OneToMany(mappedBy = "author")
    @Builder.Default
    private List<Idea> ideas = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<UserAchievement> achievements = new ArrayList<>();
}
