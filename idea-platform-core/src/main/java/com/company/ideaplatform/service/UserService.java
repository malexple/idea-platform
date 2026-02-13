package com.company.ideaplatform.service;

import com.company.ideaplatform.dto.*;
import com.company.ideaplatform.entity.Idea;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.entity.enums.UserRole;
import com.company.ideaplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final IdeaRepository ideaRepository;
    private final VoteRepository voteRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String email, String password, String displayName, Long teamId) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .displayName(displayName)
                .role(UserRole.USER)
                .build();

        if (teamId != null) {
            user.setTeam(teamRepository.findById(teamId).orElse(null));
        }

        user = userRepository.save(user);
        log.info("Created user: {}", email);

        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    public UserProfileDto getProfile(Long userId) {
        User user = findById(userId);

        long totalIdeas = ideaRepository.findByAuthorId(userId).size();
        long implementedIdeas = ideaRepository.countByAuthorIdAndStatus(userId, IdeaStatus.IMPLEMENTED);
        long votesGiven = voteRepository.countByUserId(userId);

        Map<String, Long> achievementsByHero = new HashMap<>();
        for (var heroType : com.company.ideaplatform.entity.enums.HeroType.values()) {
            long count = userAchievementRepository.countByUserIdAndHeroType(userId, heroType);
            achievementsByHero.put(heroType.name(), count);
        }

        List<UserAchievementDto> recentAchievements = userAchievementRepository
                .findRecentByUserId(userId)
                .stream()
                .map(ua -> UserAchievementDto.builder()
                        .id(ua.getId())
                        .code(ua.getAchievement().getCode())
                        .name(ua.getAchievement().getName())
                        .description(ua.getAchievement().getDescription())
                        .heroType(ua.getAchievement().getHeroType())
                        .level(ua.getAchievement().getLevel())
                        .iconPath(ua.getAchievement().getIconPath())
                        .earnedAt(ua.getEarnedAt())
                        .relatedIdeaNumber(ua.getRelatedIdea() != null
                                ? ua.getRelatedIdea().getNumber() : null)
                        .build())
                .collect(Collectors.toList());

        return UserProfileDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .teamName(user.getTeam() != null ? user.getTeam().getName() : null)
                .tribeName(user.getTeam() != null && user.getTeam().getTribe() != null
                        ? user.getTeam().getTribe().getName() : null)
                .divisionName(user.getTeam() != null && user.getTeam().getTribe() != null
                        && user.getTeam().getTribe().getDivision() != null
                        ? user.getTeam().getTribe().getDivision().getName() : null)
                .totalIdeas(totalIdeas)
                .implementedIdeas(implementedIdeas)
                .votesGiven(votesGiven)
                .achievementsByHero(achievementsByHero)
                .recentAchievements(recentAchievements)
                .build();
    }

    @Transactional
    public void updateRole(Long userId, UserRole role) {
        User user = findById(userId);
        user.setRole(role);
        userRepository.save(user);
        log.info("Updated role for user {} to {}", user.getEmail(), role);
    }

    public List<User> getReviewers() {
        return userRepository.findActiveReviewers();
    }

    public List<LeaderboardEntryDto> getLeaderboard(int limit) {
        return userRepository.findByActiveTrue().stream()
                .map(user -> {
                    List<Idea> ideas = ideaRepository.findByAuthorId(user.getId());
                    long totalVotes = ideas.stream()
                            .mapToLong(idea -> voteRepository.countByIdeaId(idea.getId()))
                            .sum();

                    return LeaderboardEntryDto.builder()
                            .userId(user.getId())
                            .displayName(user.getDisplayName())
                            .teamName(user.getTeam() != null ? user.getTeam().getName() : null)
                            .ideasCount(ideas.size())
                            .implementedCount(ideas.stream()
                                    .filter(i -> i.getStatus() == IdeaStatus.IMPLEMENTED)
                                    .count())
                            .totalVotesReceived(totalVotes)
                            .achievementsCount(userAchievementRepository.countByUserId(user.getId()))
                            .build();
                })
                .sorted((a, b) -> {
                    int cmp = Long.compare(b.getImplementedCount(), a.getImplementedCount());
                    if (cmp != 0) return cmp;
                    return Long.compare(b.getTotalVotesReceived(), a.getTotalVotesReceived());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User findOrCreateFromOAuth(String email, String displayName, String externalId, String adminEmail) {
        // Сначала ищем по externalId
        Optional<User> byExternal = userRepository.findByExternalId(externalId);
        if (byExternal.isPresent()) {
            return byExternal.get();
        }

        // Затем по email — может пользователь был предсоздан через seed
        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            user.setExternalId(externalId);
            user.setAuthProvider("keycloak");
            if (displayName != null && !displayName.isBlank()) {
                user.setDisplayName(displayName);
            }
            return userRepository.save(user);
        }

        // Создаём нового пользователя
        UserRole role = determineRoleForNewOAuthUser(email, adminEmail);

        User user = User.builder()
                .email(email)
                .displayName(displayName != null ? displayName : email)
                .externalId(externalId)
                .authProvider("keycloak")
                .role(role)
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("Created OAuth user: email={}, role={}", email, role);
        return user;
    }

    private UserRole determineRoleForNewOAuthUser(String email, String adminEmail) {
        // 1. Если email совпадает с настройкой keycloak.admin-email — ADMIN
        if (adminEmail != null && !adminEmail.isBlank() && adminEmail.equalsIgnoreCase(email)) {
            log.info("Email matches keycloak.admin-email — assigning ADMIN role to {}", email);
            return UserRole.ADMIN;
        }

        // 2. Если в БД вообще нет пользователей — первый становится ADMIN
        if (userRepository.count() == 0) {
            log.info("First user ever — assigning ADMIN role to {}", email);
            return UserRole.ADMIN;
        }

        // 3. Если нет ни одного ADMIN — тоже делаем ADMIN
        if (userRepository.findByRole(UserRole.ADMIN).isEmpty()) {
            log.info("No ADMIN users exist — assigning ADMIN role to {}", email);
            return UserRole.ADMIN;
        }

        // 4. По умолчанию — USER
        return UserRole.USER;
    }

}
