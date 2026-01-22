package com.company.ideaplatform.service;

import com.company.ideaplatform.dto.CommentCreateDto;
import com.company.ideaplatform.dto.CommentDto;
import com.company.ideaplatform.entity.Comment;
import com.company.ideaplatform.entity.Idea;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final IdeaService ideaService;
    private final EmailService emailService;
    private final AchievementService achievementService;

    @Transactional
    public CommentDto addComment(String ideaNumber, CommentCreateDto dto, User author) {
        Idea idea = ideaService.getIdeaEntityByNumber(ideaNumber);

        Comment comment = Comment.builder()
                .idea(idea)
                .author(author)
                .text(dto.getText())
                .build();

        comment = commentRepository.save(comment);

        // Notify idea author
        if (idea.getAuthor() != null && !idea.getAuthor().getId().equals(author.getId())) {
            emailService.sendCommentNotification(idea, comment);
        }

        // Check commenting achievements
        achievementService.checkCommentingAchievements(author);

        log.info("User {} added comment to idea {}", author.getEmail(), ideaNumber);

        return mapToDto(comment);
    }

    @Transactional
    public CommentDto updateComment(Long commentId, String text, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("Нельзя редактировать чужой комментарий");
        }

        comment.setText(text);
        comment.setEdited(true);
        comment = commentRepository.save(comment);

        return mapToDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("Нельзя удалить чужой комментарий");
        }

        commentRepository.delete(comment);
    }

    private CommentDto mapToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getDisplayName())
                .authorId(comment.getAuthor().getId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .edited(comment.getEdited())
                .build();
    }
}
