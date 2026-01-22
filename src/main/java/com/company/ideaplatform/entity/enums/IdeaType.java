package com.company.ideaplatform.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdeaType {
    AUTOMATION("Автоматизация", "Улучшение процессов, скрипты, упрощение рутины"),
    PROBLEM("Проблема", "Что болит — фиксация боли без готового решения"),
    IDEA("Идея", "Новый инструмент, сервис, продукт");

    private final String displayName;
    private final String description;
}
