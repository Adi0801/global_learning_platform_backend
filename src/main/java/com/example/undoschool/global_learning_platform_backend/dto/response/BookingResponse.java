package com.example.undoschool.global_learning_platform_backend.dto.response;

import com.example.undoschool.global_learning_platform_backend.entity.BookingStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID parentId,
        UUID offeringId,
        BookingStatus status,
        OffsetDateTime bookedAt,
        OfferingResponse offering
) {
}
