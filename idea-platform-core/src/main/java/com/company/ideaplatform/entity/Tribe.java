package com.company.ideaplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tribes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tribe extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id", nullable = false)
    private Division division;

    @OneToMany(mappedBy = "tribe", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Team> teams = new ArrayList<>();
}
