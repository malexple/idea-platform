package com.company.ideaplatform.service;

import com.company.ideaplatform.dto.DashboardDto;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.entity.enums.IdeaType;
import com.company.ideaplatform.repository.IdeaRepository;
import com.company.ideaplatform.repository.TeamRepository;
import com.company.ideaplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IdeaRepository ideaRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public DashboardDto getDashboard() {
        Map<String, Long> ideasByType = new HashMap<>();
        for (IdeaType type : IdeaType.values()) {
            ideasByType.put(type.name(), ideaRepository.countByType(type));
        }

        Map<String, Long> ideasByStatus = new HashMap<>();
        for (IdeaStatus status : IdeaStatus.values()) {
            ideasByStatus.put(status.name(), ideaRepository.countByStatus(status));
        }

        Map<String, Long> ideasByTeam = new HashMap<>();
        teamRepository.findByActiveTrue().forEach(team -> {
            ideasByTeam.put(team.getName(), ideaRepository.countByTeamId(team.getId()));
        });

        long totalIdeas = ideaRepository.count();
        long totalImplemented = ideaRepository.countByStatus(IdeaStatus.IMPLEMENTED);
        long totalUsers = userRepository.count();

        return DashboardDto.builder()
                .ideasByType(ideasByType)
                .ideasByStatus(ideasByStatus)
                .ideasByTeam(ideasByTeam)
                .totalIdeas(totalIdeas)
                .totalImplemented(totalImplemented)
                .totalUsers(totalUsers)
                .avgTimeToReviewDays(0) // TODO: calculate
                .avgTimeToImplementDays(0) // TODO: calculate
                .build();
    }
}
