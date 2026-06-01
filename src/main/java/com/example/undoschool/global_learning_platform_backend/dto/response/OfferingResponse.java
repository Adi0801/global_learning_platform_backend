package com.example.undoschool.global_learning_platform_backend.dto.response;

import com.example.undoschool.global_learning_platform_backend.entity.OfferingStatus;
import java.util.List;
import java.util.UUID;

public record OfferingResponse(
        UUID id,
        UUID courseId,
        String courseTitle,
        UUID teacherId,
        String teacherName,
        String title,
        String description,
        String teacherTimeZone,
        OfferingStatus status,
        List<SessionResponse> sessions
) {
}
