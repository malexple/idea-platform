package com.company.ideaplatform.entity;

import com.company.ideaplatform.entity.enums.AchievementLevel;
import com.company.ideaplatform.entity.enums.HeroType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "hero_type", nullable = false)
    private HeroType heroType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementLevel level;

    @Column(name = "icon_path")
    private String iconPath;

    private Integer threshold;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
