package com.company.ideaplatform.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoteType {
    MUST_HAVE("Очень нужно", 5, true),
    CONVENIENT("Удобно", 4, true),
    USEFUL("Будет полезно", 3, true),
    FULLY_SUPPORT("Всеми руками за", 5, true),
    INTERESTING("Интересная идея", 2, true),
    NEEDS_IMPROVEMENT("Стоит доработать", 0, false),
    NOT_GREAT("Не очень удачная идея", -1, false);

    private final String displayName;
    private final int weight;
    private final boolean positive;
}
