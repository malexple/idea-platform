package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.Comment;
import com.company.ideaplatform.entity.Idea;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public void sendIdeaCreatedNotification(Idea idea, User author) {
        if (idea.getAnonymous() || author == null) {
            return;
        }

        String subject = String.format("Заявка %s создана", idea.getNumber());
        String text = String.format("""
                Здравствуйте, %s!
                
                Ваша заявка успешно создана и будет рассмотрена в ближайшее время.
                
                Номер заявки: %s
                Тип: %s
                Заголовок: %s
                
                Срок рассмотрения: до %s
                
                Следить за статусом можно по ссылке: %s/ideas/%s
                
                С уважением,
                Платформа идей
                """,
                author.getDisplayName(),
                idea.getNumber(),
                idea.getType().getDisplayName(),
                idea.getTitle(),
                idea.getReviewDeadline().toLocalDate(),
                baseUrl,
                idea.getNumber()
        );

        sendEmail(author.getEmail(), subject, text);
    }

    @Async
    public void notifyReviewersAboutNewIdea(Idea idea) {
        List<User> reviewers = userRepository.findActiveReviewers();

        String subject = String.format("Новая заявка %s требует рассмотрения", idea.getNumber());
        String text = String.format("""
                Поступила новая заявка, требующая рассмотрения.
                
                Номер: %s
                Тип: %s
                Заголовок: %s
                Команда: %s
                
                Рассмотреть заявку: %s/review/%s
                
                Срок рассмотрения: до %s
                """,
                idea.getNumber(),
                idea.getType().getDisplayName(),
                idea.getTitle(),
                idea.getTeam().getName(),
                baseUrl,
                idea.getNumber(),
                idea.getReviewDeadline().toLocalDate()
        );

        for (User reviewer : reviewers) {
            sendEmail(reviewer.getEmail(), subject, text);
        }
    }

    @Async
    public void sendStatusChangeNotification(Idea idea, IdeaStatus oldStatus, IdeaStatus newStatus) {
        if (idea.getAuthor() == null) {
            return;
        }

        String subject = String.format("Статус заявки %s изменён", idea.getNumber());
        String text = String.format("""
                Здравствуйте, %s!
                
                Статус вашей заявки изменён.
                
                Номер заявки: %s
                Заголовок: %s
                Предыдущий статус: %s
                Новый статус: %s
                
                Подробности: %s/ideas/%s
                
                С уважением,
                Платформа идей
                """,
                idea.getAuthor().getDisplayName(),
                idea.getNumber(),
                idea.getTitle(),
                oldStatus != null ? oldStatus.getDisplayName() : "—",
                newStatus.getDisplayName(),
                baseUrl,
                idea.getNumber()
        );

        sendEmail(idea.getAuthor().getEmail(), subject, text);
    }

    @Async
    public void sendCommentNotification(Idea idea, Comment comment) {
        if (idea.getAuthor() == null) {
            return;
        }

        String subject = String.format("Новый комментарий к заявке %s", idea.getNumber());
        String text = String.format("""
                Здравствуйте, %s!
                
                К вашей заявке добавлен комментарий.
                
                Номер заявки: %s
                Заголовок: %s
                
                Автор комментария: %s
                Комментарий: %s
                
                Посмотреть: %s/ideas/%s
                
                С уважением,
                Платформа идей
                """,
                idea.getAuthor().getDisplayName(),
                idea.getNumber(),
                idea.getTitle(),
                comment.getAuthor().getDisplayName(),
                comment.getText().length() > 200
                        ? comment.getText().substring(0, 200) + "..."
                        : comment.getText(),
                baseUrl,
                idea.getNumber()
        );

        sendEmail(idea.getAuthor().getEmail(), subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.debug("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
