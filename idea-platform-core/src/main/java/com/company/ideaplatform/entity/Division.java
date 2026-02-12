package com.company.ideaplatform.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "divisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Division extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "division", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Tribe> tribes = new ArrayList<>();
}
