package com.project.edusync.adm.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for creating or updating a Subject.
 */
@Data
public class SubjectRequestDto {

    @NotBlank(message = "Subject name cannot be blank")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Subject code cannot be blank")
    @Size(max = 50)
    private String subjectCode;

    @Size(max = 100)
    private String requiresSpecialRoomType; // e.g., "LAB", "MUSIC_ROOM"

    @Size(max = 50)
    private String color;
}