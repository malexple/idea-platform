package com.company.ideaplatform.dto;

import com.company.ideaplatform.entity.enums.IdeaType;
import com.company.ideaplatform.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class IdeaCreateDto {

    @NotNull(message = "Тип заявки обязателен")
    private IdeaType type;

    @NotBlank(message = "Заголовок обязателен")
    @Size(max = 500, message = "Заголовок не должен превышать 500 символов")
    private String title;

    @NotBlank(message = "Описание обязательно")
    private String description;

    @NotBlank(message = "Ожидаемый эффект обязателен")
    private String expectedEffect;

    @NotNull(message = "Приоритет обязателен")
    private Priority priority;

    @NotNull(message = "Дивизион обязателен")
    private Long divisionId;

    @NotNull(message = "Трайб обязателен")
    private Long tribeId;

    @NotNull(message = "Команда обязательна")
    private Long teamId;

    private boolean anonymous;

    private List<MultipartFile> attachments;
}
