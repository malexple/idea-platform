package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.Attachment;
import com.company.ideaplatform.entity.Idea;
import com.company.ideaplatform.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    @Value("${app.upload.allowed-types:pdf,docx,xlsx,png,jpg,jpeg,zip}")
    private String allowedTypes;

    @Value("${app.upload.max-file-size:10485760}") // 10 MB
    private long maxFileSize;

    public Attachment saveAttachment(Idea idea, MultipartFile file) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String storedName = UUID.randomUUID() + "." + extension;

        Path uploadDir = Paths.get(uploadPath, idea.getNumber());
        try {
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = Attachment.builder()
                    .idea(idea)
                    .fileName(storedName)
                    .originalName(originalName)
                    .fileType(file.getContentType())
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .build();

            attachment = attachmentRepository.save(attachment);
            log.info("Saved attachment {} for idea {}", originalName, idea.getNumber());

            return attachment;

        } catch (IOException e) {
            log.error("Failed to save attachment: {}", e.getMessage());
            throw new RuntimeException("Не удалось сохранить файл: " + originalName, e);
        }
    }

    public Resource loadAttachment(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Вложение не найдено"));

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Файл не найден или недоступен");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Ошибка при загрузке файла", e);
        }
    }

    public Attachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Вложение не найдено"));
    }

    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = getAttachment(attachmentId);

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", attachment.getFilePath());
        }

        attachmentRepository.delete(attachment);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Имя файла не указано");
        }

        // Проверка расширения
        String extension = getExtension(originalName);
        List<String> allowed = Arrays.asList(allowedTypes.toLowerCase().split(","));

        if (extension.isEmpty() || !allowed.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    String.format("Недопустимый тип файла '%s'. Разрешены: %s",
                            originalName, String.join(", ", allowed)));
        }

        // Проверка размера
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("Файл '%s' превышает максимальный размер %d МБ",
                            originalName, maxFileSize / (1024 * 1024)));
        }

        // Проверка на опасные расширения (двойные расширения)
        String lowerName = originalName.toLowerCase();
        List<String> dangerous = Arrays.asList(".exe", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".jar");
        for (String ext : dangerous) {
            if (lowerName.contains(ext)) {
                throw new IllegalArgumentException(
                        String.format("Файл '%s' содержит запрещённое расширение", originalName));
            }
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
