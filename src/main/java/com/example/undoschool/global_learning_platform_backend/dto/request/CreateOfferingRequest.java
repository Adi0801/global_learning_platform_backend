package com.example.undoschool.global_learning_platform_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateOfferingRequest(
        @NotNull UUID courseId,
        @NotNull UUID teacherId,
        @NotBlank @Size(max = 160) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 64) String teacherTimeZone
) {
}
