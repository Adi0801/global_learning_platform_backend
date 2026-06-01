package com.example.undoschool.global_learning_platform_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookOfferingRequest(
        @NotNull UUID parentId,
        @NotNull UUID offeringId
) {
}
