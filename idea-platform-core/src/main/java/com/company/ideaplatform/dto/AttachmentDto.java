package com.company.ideaplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentDto {
    private Long id;
    private String fileName;
    private String originalName;
    private String fileType;
    private Long fileSize;
}
