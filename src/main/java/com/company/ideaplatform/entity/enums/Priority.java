package com.company.ideaplatform.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Priority {
    CRITICAL("Критично", 1),
    DESIRED("Желательно", 2),
    NICE_TO_HAVE("Было бы неплохо", 3);

    private final String displayName;
    private final int weight;
}
