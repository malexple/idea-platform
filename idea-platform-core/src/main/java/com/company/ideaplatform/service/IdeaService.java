package com.company.ideaplatform.service;

import com.company.ideaplatform.dto.*;
import com.company.ideaplatform.entity.*;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.entity.enums.IdeaType;
import com.company.ideaplatform.entity.enums.VoteType;
import com.company.ideaplatform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final UserRepository userRepository;
    private final DivisionRepository divisionRepository;
    private final TribeRepository tribeRepository;
    private final TeamRepository teamRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final AttachmentService attachmentService;
    private final EmailService emailService;
    private final AchievementService achievementService;

    @Value("${app.sla.review-days:5}")
    private int reviewDays;

    @Transactional
    public Idea createIdea(IdeaCreateDto dto, User author) {
        Idea idea = new Idea();
        idea.setNumber(generateNumber());
        idea.setType(dto.getType());
        idea.setTitle(dto.getTitle());
        idea.setDescription(dto.getDescription());
        idea.setExpectedEffect(dto.getExpectedEffect());
        idea.setPriority(dto.getPriority());
        idea.setAnonymous(dto.isAnonymous());
        idea.setAuthor(dto.isAnonymous() ? null : author);
        idea.setStatus(IdeaStatus.SUBMITTED);
        idea.setReviewDeadline(LocalDateTime.now().plusDays(reviewDays));

        Division division = divisionRepository.findById(dto.getDivisionId())
                .orElseThrow(() -> new IllegalArgumentException("Дивизион не найден"));
        Tribe tribe = tribeRepository.findById(dto.getTribeId())
                .orElseThrow(() -> new IllegalArgumentException("Трайб не найден"));
        Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));

        idea.setDivision(division);
        idea.setTribe(tribe);
        idea.setTeam(team);

        idea = ideaRepository.save(idea);

        // Save attachments
        if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
            for (MultipartFile file : dto.getAttachments()) {
                if (!file.isEmpty()) {
                    attachmentService.saveAttachment(idea, file);
                }
            }
        }

        // Create status history
        createStatusHistory(idea, null, IdeaStatus.SUBMITTED, author, "Заявка создана");

        // Send notifications
        emailService.sendIdeaCreatedNotification(idea, author);
        emailService.notifyReviewersAboutNewIdea(idea);

        // Check achievements
        if (!dto.isAnonymous()) {
            achievementService.checkAndAwardAchievements(author, idea);
        }

        log.info("Created idea {} by user {}", idea.getNumber(),
                dto.isAnonymous() ? "anonymous" : author.getEmail());

        return idea;
    }

    public Page<IdeaViewDto> getIdeasForVoting(Pageable pageable, Long currentUserId) {
        Page<Idea> ideas = ideaRepository.findByStatus(IdeaStatus.VOTING, pageable);
        return ideas.map(idea -> mapToViewDto(idea, currentUserId));
    }

    public Page<IdeaViewDto> getIdeasByStatus(IdeaStatus status, Pageable pageable, Long currentUserId) {
        Page<Idea> ideas = ideaRepository.findByStatus(status, pageable);
        return ideas.map(idea -> mapToViewDto(idea, currentUserId));
    }

    public Page<IdeaViewDto> getIdeasByType(IdeaType type, Pageable pageable, Long currentUserId) {
        Page<Idea> ideas = ideaRepository.findByType(type, pageable);
        return ideas.map(idea -> mapToViewDto(idea, currentUserId));
    }

    public Page<IdeaViewDto> getMyIdeas(Long authorId, Pageable pageable) {
        Page<Idea> ideas = ideaRepository.findByAuthorId(authorId, pageable);
        return ideas.map(idea -> mapToViewDto(idea, authorId));
    }

    public Page<IdeaViewDto> getIdeasForReview(Pageable pageable, Long currentUserId) {
        List<IdeaStatus> reviewStatuses = List.of(IdeaStatus.SUBMITTED, IdeaStatus.ON_REVIEW);
        Page<Idea> ideas = ideaRepository.findByStatusIn(reviewStatuses, pageable);
        return ideas.map(idea -> mapToViewDto(idea, currentUserId));
    }

    public IdeaViewDto getIdeaByNumber(String number, Long currentUserId) {
        Idea idea = ideaRepository.findByNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + number));
        return mapToViewDto(idea, currentUserId);
    }

    public Idea getIdeaEntityByNumber(String number) {
        return ideaRepository.findByNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + number));
    }

    @Transactional
    public void vote(String ideaNumber, User user, VoteType voteType) {
        Idea idea = getIdeaEntityByNumber(ideaNumber);

        if (idea.getStatus() != IdeaStatus.VOTING) {
            throw new IllegalStateException("Голосование доступно только для заявок в статусе 'Голосование'");
        }

        Optional<Vote> existingVote = voteRepository.findByIdeaIdAndUserId(idea.getId(), user.getId());

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            vote.setVoteType(voteType);
            voteRepository.save(vote);
        } else {
            Vote vote = Vote.builder()
                    .idea(idea)
                    .user(user)
                    .voteType(voteType)
                    .build();
            voteRepository.save(vote);

            // Check achievements for voting
            achievementService.checkVotingAchievements(user);
        }

        log.info("User {} voted {} for idea {}", user.getEmail(), voteType, ideaNumber);
    }

    @Transactional
    public void removeVote(String ideaNumber, User user) {
        Idea idea = getIdeaEntityByNumber(ideaNumber);
        voteRepository.findByIdeaIdAndUserId(idea.getId(), user.getId())
                .ifPresent(voteRepository::delete);
    }

    @Transactional
    public void processReviewDecision(String ideaNumber, User reviewer, ReviewDecisionDto dto) {
        Idea idea = getIdeaEntityByNumber(ideaNumber);
        IdeaStatus oldStatus = idea.getStatus();
        IdeaStatus newStatus = dto.getDecision();

        // Handle duplicate
        if (newStatus == IdeaStatus.DUPLICATE && dto.getDuplicateOfIdeaId() != null) {
            Idea parentIdea = ideaRepository.findById(dto.getDuplicateOfIdeaId())
                    .orElseThrow(() -> new IllegalArgumentException("Родительская заявка не найдена"));
            idea.setParentIdea(parentIdea);
        }

        // If approved, move to voting
        if (newStatus == IdeaStatus.APPROVED) {
            newStatus = IdeaStatus.VOTING;
        }

        idea.setStatus(newStatus);
        ideaRepository.save(idea);

        createStatusHistory(idea, oldStatus, newStatus, reviewer, dto.getComment());

        // Notify author
        if (idea.getAuthor() != null) {
            emailService.sendStatusChangeNotification(idea, oldStatus, newStatus);
        }

        log.info("Reviewer {} set status {} for idea {}", reviewer.getEmail(), newStatus, ideaNumber);
    }

    @Transactional
    public void removeFromDuplicates(String ideaNumber, User user) {
        Idea idea = getIdeaEntityByNumber(ideaNumber);

        if (idea.getStatus() != IdeaStatus.DUPLICATE) {
            throw new IllegalStateException("Заявка не является дубликатом");
        }

        idea.setParentIdea(null);
        idea.setStatus(IdeaStatus.VOTING);
        ideaRepository.save(idea);

        createStatusHistory(idea, IdeaStatus.DUPLICATE, IdeaStatus.VOTING, user,
                "Убрано из дубликатов, перемещено в голосование");
    }

    @Transactional
    public void changeStatus(String ideaNumber, IdeaStatus newStatus, User user, String comment) {
        Idea idea = getIdeaEntityByNumber(ideaNumber);
        IdeaStatus oldStatus = idea.getStatus();

        idea.setStatus(newStatus);
        ideaRepository.save(idea);

        createStatusHistory(idea, oldStatus, newStatus, user, comment);

        if (idea.getAuthor() != null) {
            emailService.sendStatusChangeNotification(idea, oldStatus, newStatus);

            // Check implementation achievements
            if (newStatus == IdeaStatus.IMPLEMENTED) {
                achievementService.checkImplementationAchievements(idea.getAuthor(), idea);
            }
        }
    }

    @Transactional
    public void processOverdueReviews() {
        List<Idea> overdueIdeas = ideaRepository.findOverdueForReview(
                IdeaStatus.SUBMITTED, LocalDateTime.now());

        for (Idea idea : overdueIdeas) {
            IdeaStatus oldStatus = idea.getStatus();
            idea.setStatus(IdeaStatus.VOTING);
            ideaRepository.save(idea);

            createStatusHistory(idea, oldStatus, IdeaStatus.VOTING, null,
                    "Автоматический перевод в голосование по истечении SLA");

            if (idea.getAuthor() != null) {
                emailService.sendStatusChangeNotification(idea, oldStatus, IdeaStatus.VOTING);
            }

            log.info("Idea {} moved to VOTING due to SLA expiration", idea.getNumber());
        }
    }

    public Page<IdeaViewDto> getTopVotedIdeas(Pageable pageable, Long currentUserId) {
        Page<Idea> ideas = ideaRepository.findTopVotedIdeas(pageable);
        return ideas.map(idea -> mapToViewDto(idea, currentUserId));
    }

    private String generateNumber() {
        String prefix = "IDEA-";
        Optional<String> lastNumber = ideaRepository.findLastNumber();

        if (lastNumber.isPresent()) {
            String num = lastNumber.get().replace(prefix, "");
            int next = Integer.parseInt(num) + 1;
            return prefix + String.format("%06d", next);
        }

        return prefix + "000001";
    }

    private void createStatusHistory(Idea idea, IdeaStatus fromStatus, IdeaStatus toStatus,
                                     User changedBy, String comment) {
        StatusHistory history = StatusHistory.builder()
                .idea(idea)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .comment(comment)
                .build();
        statusHistoryRepository.save(history);
    }

    private IdeaViewDto mapToViewDto(Idea idea, Long currentUserId) {
        Map<String, Long> voteCounts = getVoteCounts(idea.getId());
        String currentUserVote = null;

        if (currentUserId != null) {
            currentUserVote = voteRepository.findByIdeaIdAndUserId(idea.getId(), currentUserId)
                    .map(v -> v.getVoteType().name())
                    .orElse(null);
        }

        List<CommentDto> comments = commentRepository.findByIdeaIdOrderByCreatedAtAsc(idea.getId())
                .stream()
                .map(this::mapToCommentDto)
                .collect(Collectors.toList());

        List<AttachmentDto> attachments = idea.getAttachments().stream()
                .map(this::mapToAttachmentDto)
                .collect(Collectors.toList());

        return IdeaViewDto.builder()
                .id(idea.getId())
                .number(idea.getNumber())
                .type(idea.getType())
                .title(idea.getTitle())
                .description(idea.getDescription())
                .expectedEffect(idea.getExpectedEffect())
                .priority(idea.getPriority())
                .status(idea.getStatus())
                .anonymous(idea.getAnonymous())
                .authorName(idea.getAnonymous() || idea.getAuthor() == null
                        ? "Аноним" : idea.getAuthor().getDisplayName())
                .authorId(idea.getAuthor() != null ? idea.getAuthor().getId() : null)
                .divisionName(idea.getDivision().getName())
                .tribeName(idea.getTribe().getName())
                .teamName(idea.getTeam().getName())
                .createdAt(idea.getCreatedAt())
                .reviewDeadline(idea.getReviewDeadline())
                .jiraLink(idea.getJiraLink())
                .parentIdeaNumber(idea.getParentIdea() != null ? idea.getParentIdea().getNumber() : null)
                .parentIdeaId(idea.getParentIdea() != null ? idea.getParentIdea().getId() : null)
                .attachments(attachments)
                .comments(comments)
                .votesCounts(voteCounts)
                .totalVotes(voteCounts.values().stream().mapToLong(Long::longValue).sum())
                .currentUserVote(currentUserVote)
                .actualEffect(idea.getActualEffect())
                .build();
    }

    private Map<String, Long> getVoteCounts(Long ideaId) {
        List<Object[]> results = voteRepository.countByIdeaIdGroupByVoteType(ideaId);
        Map<String, Long> counts = new HashMap<>();

        for (VoteType type : VoteType.values()) {
            counts.put(type.name(), 0L);
        }

        for (Object[] row : results) {
            VoteType type = (VoteType) row[0];
            Long count = (Long) row[1];
            counts.put(type.name(), count);
        }

        return counts;
    }

    private CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getDisplayName())
                .authorId(comment.getAuthor().getId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .edited(comment.getEdited())
                .build();
    }

    private AttachmentDto mapToAttachmentDto(Attachment attachment) {
        return AttachmentDto.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .originalName(attachment.getOriginalName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .build();
    }

    /**
     * Продвижение идеи из VOTING в работу
     */
    @Transactional
    public void advanceToInProgress(String ideaNumber, String jiraLink, User changedBy) {
        if (jiraLink == null || jiraLink.isBlank()) {
            throw new IllegalArgumentException("Для перевода в работу необходимо указать ссылку на задачу в Jira");
        }

        Idea idea = getIdeaEntityByNumber(ideaNumber);

        if (idea.getStatus() != IdeaStatus.VOTING && idea.getStatus() != IdeaStatus.APPROVED) {
            throw new IllegalStateException("Перевести в работу можно только из статуса 'Голосование' или 'Одобрена'");
        }

        IdeaStatus oldStatus = idea.getStatus();
        idea.setStatus(IdeaStatus.IN_PROGRESS);
        idea.setJiraLink(jiraLink);
        ideaRepository.save(idea);

        createStatusHistory(idea, oldStatus, IdeaStatus.IN_PROGRESS, changedBy, "Взято в работу: " + jiraLink);

        if (idea.getAuthor() != null) {
            emailService.sendStatusChangeNotification(idea, oldStatus, IdeaStatus.IN_PROGRESS);
        }

        log.info("Idea {} advanced to IN_PROGRESS by {}", ideaNumber, changedBy.getEmail());
    }

    /**
     * Перевод в пилот
     */
    @Transactional
    public void advanceToPilot(String ideaNumber, String comment, User changedBy) {
        Idea idea = getIdeaEntityByNumber(ideaNumber);

        if (idea.getStatus() != IdeaStatus.IN_PROGRESS) {
            throw new IllegalStateException("Перевести в пилот можно только из статуса 'В работе'");
        }

        IdeaStatus oldStatus = idea.getStatus();
        idea.setStatus(IdeaStatus.PILOT);
        ideaRepository.save(idea);

        createStatusHistory(idea, oldStatus, IdeaStatus.PILOT, changedBy, comment);

        if (idea.getAuthor() != null) {
            emailService.sendStatusChangeNotification(idea, oldStatus, IdeaStatus.PILOT);
        }

        log.info("Idea {} advanced to PILOT by {}", ideaNumber, changedBy.getEmail());
    }

    /**
     * Завершение — внедрено
     */
    @Transactional
    public void completeIdea(String ideaNumber, String actualEffect, User changedBy) {
        if (actualEffect == null || actualEffect.isBlank()) {
            throw new IllegalArgumentException("Для завершения необходимо указать фактический эффект");
        }

        Idea idea = getIdeaEntityByNumber(ideaNumber);

        if (idea.getStatus() != IdeaStatus.PILOT && idea.getStatus() != IdeaStatus.IN_PROGRESS) {
            throw new IllegalStateException("Завершить можно только из статуса 'Пилот' или 'В работе'");
        }

        IdeaStatus oldStatus = idea.getStatus();
        idea.setStatus(IdeaStatus.IMPLEMENTED);
        idea.setActualEffect(actualEffect);
        ideaRepository.save(idea);

        createStatusHistory(idea, oldStatus, IdeaStatus.IMPLEMENTED, changedBy, "Фактический эффект: " + actualEffect);

        // Награда автору за реализацию
        if (idea.getAuthor() != null) {
            achievementService.checkImplementationAchievements(idea.getAuthor(), idea);
            emailService.sendStatusChangeNotification(idea, oldStatus, IdeaStatus.IMPLEMENTED);
        }

        log.info("Idea {} completed by {}", ideaNumber, changedBy.getEmail());
    }

    /**
     * Отклонение/откладывание из любого статуса
     */
    @Transactional
    public void rejectOrPostpone(String ideaNumber, IdeaStatus newStatus, String comment, User changedBy) {
        if (newStatus != IdeaStatus.REJECTED && newStatus != IdeaStatus.POSTPONED) {
            throw new IllegalArgumentException("Допустимые статусы: REJECTED, POSTPONED");
        }

        Idea idea = getIdeaEntityByNumber(ideaNumber);
        IdeaStatus oldStatus = idea.getStatus();

        idea.setStatus(newStatus);
        ideaRepository.save(idea);

        createStatusHistory(idea, oldStatus, newStatus, changedBy, comment);

        if (idea.getAuthor() != null) {
            emailService.sendStatusChangeNotification(idea, oldStatus, newStatus);
        }

        log.info("Idea {} changed to {} by {}", ideaNumber, newStatus, changedBy.getEmail());
    }

}
