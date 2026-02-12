package com.company.ideaplatform.service;

import com.company.ideaplatform.dto.*;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final UserRepository userRepository;
    private final IdeaRepository ideaRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final TeamRepository teamRepository;

    // Пороги для классификации
    private static final int INNOVATOR_IDEAS_THRESHOLD = 3;
    private static final int INNOVATOR_IMPLEMENTED_THRESHOLD = 1;
    private static final int EARLY_ADOPTER_VOTES_THRESHOLD = 10;
    private static final int EARLY_ADOPTER_COMMENTS_THRESHOLD = 5;
    private static final int PARTICIPANT_ACTIVITY_THRESHOLD = 1;

    @Transactional(readOnly = true)
    public ManagerDashboardDto getDashboard() {
        List<User> allUsers = userRepository.findByActiveTrue();
        List<InnovatorDto> classifiedUsers = classifyAllUsers(allUsers);

        long innovators = classifiedUsers.stream()
                .filter(u -> "INNOVATOR".equals(u.getCategory())).count();
        long earlyAdopters = classifiedUsers.stream()
                .filter(u -> "EARLY_ADOPTER".equals(u.getCategory())).count();
        long participants = classifiedUsers.stream()
                .filter(u -> "PARTICIPANT".equals(u.getCategory())).count();
        long observers = classifiedUsers.stream()
                .filter(u -> "OBSERVER".equals(u.getCategory())).count();
        long inactive = classifiedUsers.stream()
                .filter(u -> "INACTIVE".equals(u.getCategory())).count();

        long totalIdeas = ideaRepository.count();
        long implemented = ideaRepository.countByStatus(IdeaStatus.IMPLEMENTED);

        return ManagerDashboardDto.builder()
                .totalUsers(allUsers.size())
                .activeUsers(innovators + earlyAdopters + participants)
                .totalIdeas(totalIdeas)
                .implementedIdeas(implemented)
                .innovatorsCount(innovators)
                .earlyAdoptersCount(earlyAdopters)
                .participantsCount(participants)
                .observersCount(observers)
                .inactiveCount(inactive)
                .funnel(buildFunnel())
                .submittedToImplementedRate(totalIdeas > 0 ? (double) implemented / totalIdeas * 100 : 0)
                .avgDaysToImplement(0) // TODO: вычислить из statusHistory
                .topInnovators(getTopInnovators(classifiedUsers, 10))
                .topTeams(getTopTeams(10))
                .build();
    }

    @Transactional(readOnly = true)
    public List<InnovatorDto> classifyAllUsers(List<User> users) {
        return users.stream()
                .map(this::classifyUser)
                .sorted((a, b) -> Double.compare(b.getActivityScore(), a.getActivityScore()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InnovatorDto> getPeopleByCategory(String category) {
        List<User> allUsers = userRepository.findByActiveTrue();
        List<InnovatorDto> classified = classifyAllUsers(allUsers);

        if (category == null || "ALL".equals(category)) {
            return classified;
        }

        return classified.stream()
                .filter(u -> category.equals(u.getCategory()))
                .collect(Collectors.toList());
    }

    private InnovatorDto classifyUser(User user) {
        Long userId = user.getId();

        long ideasCount = ideaRepository.findByAuthorId(userId).size();
        long implementedCount = ideaRepository.countByAuthorIdAndStatus(userId, IdeaStatus.IMPLEMENTED);
        long votesCount = voteRepository.countByUserId(userId);
        long commentsCount = commentRepository.countByAuthorId(userId);

        String category = determineCategory(ideasCount, implementedCount, votesCount, commentsCount);
        double activityScore = calculateActivityScore(ideasCount, implementedCount, votesCount, commentsCount);

        return InnovatorDto.builder()
                .userId(userId)
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .teamName(user.getTeam() != null ? user.getTeam().getName() : null)
                .category(category)
                .ideasCount(ideasCount)
                .implementedCount(implementedCount)
                .votesCount(votesCount)
                .commentsCount(commentsCount)
                .activityScore(activityScore)
                .build();
    }

    private String determineCategory(long ideas, long implemented, long votes, long comments) {
        // Инноватор: ≥3 идеи ИЛИ ≥1 реализованная
        if (ideas >= INNOVATOR_IDEAS_THRESHOLD || implemented >= INNOVATOR_IMPLEMENTED_THRESHOLD) {
            return "INNOVATOR";
        }

        // Ранний последователь: ≥10 голосов ИЛИ ≥5 комментариев
        if (votes >= EARLY_ADOPTER_VOTES_THRESHOLD || comments >= EARLY_ADOPTER_COMMENTS_THRESHOLD) {
            return "EARLY_ADOPTER";
        }

        // Участник: хотя бы 1 активность
        if (ideas > 0 || votes > 0 || comments > 0) {
            return "PARTICIPANT";
        }

        // Наблюдатель: зарегистрирован, но не активен
        // TODO: добавить lastLoginAt в User для различия OBSERVER/INACTIVE
        return "OBSERVER";
    }

    private double calculateActivityScore(long ideas, long implemented, long votes, long comments) {
        // Взвешенный скор активности
        return ideas * 10 + implemented * 50 + votes * 2 + comments * 3;
    }

    private List<FunnelStageDto> buildFunnel() {
        List<FunnelStageDto> funnel = new ArrayList<>();
        long total = ideaRepository.count();

        for (IdeaStatus status : IdeaStatus.values()) {
            if (status == IdeaStatus.DRAFT) continue; // Черновики не показываем

            long count = ideaRepository.countByStatus(status);
            funnel.add(FunnelStageDto.builder()
                    .status(status.name())
                    .displayName(status.getDisplayName())
                    .count(count)
                    .percentage(total > 0 ? (double) count / total * 100 : 0)
                    .avgDaysInStage(0) // TODO: вычислить
                    .build());
        }

        return funnel;
    }

    private List<InnovatorDto> getTopInnovators(List<InnovatorDto> all, int limit) {
        return all.stream()
                .filter(u -> "INNOVATOR".equals(u.getCategory()) || "EARLY_ADOPTER".equals(u.getCategory()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamActivityDto> getTopTeams(int limit) {
        return teamRepository.findByActiveTrue().stream()
                .map(team -> {
                    long members = team.getMembers() != null ? team.getMembers().size() : 0;
                    long ideas = ideaRepository.countByTeamId(team.getId());
                    long implemented = 0; // TODO: добавить метод в репозиторий

                    // Считаем активных пользователей в команде
                    long activeUsers = team.getMembers() != null ?
                            team.getMembers().stream()
                                    .filter(u -> {
                                        long userIdeas = ideaRepository.findByAuthorId(u.getId()).size();
                                        long userVotes = voteRepository.countByUserId(u.getId());
                                        return userIdeas > 0 || userVotes > 0;
                                    })
                                    .count() : 0;

                    return TeamActivityDto.builder()
                            .teamId(team.getId())
                            .teamName(team.getName())
                            .tribeName(team.getTribe() != null ? team.getTribe().getName() : null)
                            .divisionName(team.getTribe() != null && team.getTribe().getDivision() != null
                                    ? team.getTribe().getDivision().getName() : null)
                            .membersCount(members)
                            .ideasCount(ideas)
                            .implementedCount(implemented)
                            .activeUsersCount(activeUsers)
                            .engagementRate(members > 0 ? (double) activeUsers / members * 100 : 0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getIdeasCount(), a.getIdeasCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
