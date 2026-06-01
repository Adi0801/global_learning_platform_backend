package com.example.undoschool.global_learning_platform_backend.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID offeringId,
        UUID teacherId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String timeZone
) {
}
