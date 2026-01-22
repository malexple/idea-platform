package com.company.ideaplatform.config;

import com.company.ideaplatform.service.IdeaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final IdeaService ideaService;

    /**
     * Проверка просроченных заявок каждый час
     * Автоматический перевод в голосование по истечении SLA
     */
    @Scheduled(cron = "0 0 * * * *")
    public void processOverdueReviews() {
        log.info("Running scheduled task: processOverdueReviews");
        ideaService.processOverdueReviews();
    }
}
