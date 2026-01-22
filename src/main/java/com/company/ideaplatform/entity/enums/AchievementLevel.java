package com.company.ideaplatform.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AchievementLevel {
    NOVICE("Новичок", 1, "#CD7F32"),
    EXPLORER("Исследователь", 2, "#C0C0C0"),
    MASTER("Мастер", 3, "#FFD700"),
    EXPERT("Эксперт", 4, "#E5E4E2"),
    LEGEND("Легенда", 5, "#B9F2FF"),
    SPECIAL("Особая", 0, "#FF6B6B");

    private final String displayName;
    private final int tier;
    private final String frameColor;
}
