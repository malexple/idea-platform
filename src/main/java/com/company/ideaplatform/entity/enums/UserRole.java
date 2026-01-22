package com.company.ideaplatform.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("Пользователь"),
    REVIEWER("Ревьюер"),
    MANAGER("Руководитель"),
    ADMIN("Администратор");

    private final String displayName;
}
