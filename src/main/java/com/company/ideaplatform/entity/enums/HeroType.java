package com.company.ideaplatform.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HeroType {
    OWL("Сова", "Мудрость, анализ", "#8B5CF6"),
    BEE("Пчела", "Трудолюбие, оптимизация", "#F59E0B"),
    EAGLE("Орёл", "Зоркость, видение", "#3B82F6"),
    PHOENIX("Феникс", "Возрождение, внедрение", "#EF4444"),
    DOLPHIN("Дельфин", "Коммуникация, поддержка", "#06B6D4"),
    DRAGON("Чёрный дракон", "Сила, влияние, власть", "#1F2937");

    private final String displayName;
    private final String description;
    private final String color;
}
