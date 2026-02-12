package com.company.ideaplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDto {

    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(max = 5000, message = "Комментарий не должен превышать 5000 символов")
    private String text;
}
