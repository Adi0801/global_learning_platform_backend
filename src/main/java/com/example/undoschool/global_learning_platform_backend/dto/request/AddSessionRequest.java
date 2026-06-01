package com.example.undoschool.global_learning_platform_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AddSessionRequest(
        @NotNull LocalDateTime startDateTime,
        @NotNull LocalDateTime endDateTime,
        @Size(max = 64) String timeZone
) {
}
