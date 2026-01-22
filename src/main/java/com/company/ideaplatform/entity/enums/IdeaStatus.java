package com.company.ideaplatform.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdeaStatus {
    DRAFT("Черновик"),
    SUBMITTED("Отправлена"),
    ON_REVIEW("На рассмотрении"),
    DUPLICATE("Дубликат"),
    APPROVED("Одобрена"),
    REJECTED("Отклонена"),
    POSTPONED("Отложена"),
    VOTING("Голосование"),
    IN_PROGRESS("В работе"),
    PILOT("Пилот"),
    IMPLEMENTED("Внедрено"),
    CLOSED("Закрыта");

    private final String displayName;
}
