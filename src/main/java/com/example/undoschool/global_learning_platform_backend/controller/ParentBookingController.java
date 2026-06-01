package com.example.undoschool.global_learning_platform_backend.controller;

import com.example.undoschool.global_learning_platform_backend.dto.request.BookOfferingRequest;
import com.example.undoschool.global_learning_platform_backend.dto.response.BookingResponse;
import com.example.undoschool.global_learning_platform_backend.dto.response.OfferingResponse;
import com.example.undoschool.global_learning_platform_backend.service.ParentBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/parent")
@Tag(name = "Parent Bookings", description = "APIs for parents to view offerings and book full offerings.")
public class ParentBookingController {

    private final ParentBookingService parentBookingService;

    @GetMapping("/offerings")
    @Operation(summary = "Get available offerings", description = "Returns published offerings with all session times converted to the parent's timezone.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offerings returned"),
            @ApiResponse(responseCode = "400", description = "Invalid or missing timezone")
    })
    public List<OfferingResponse> getAvailableOfferings(
            @Parameter(example = "Asia/Kolkata", description = "Parent IANA timezone")
            @RequestParam @NotBlank String timeZone
    ) {
        return parentBookingService.getAvailableOfferings(timeZone);
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Book offering", description = "Books the complete offering for a parent after checking all session-level conflicts inside a transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking confirmed",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Offering is unavailable or has no sessions"),
            @ApiResponse(responseCode = "404", description = "Parent or offering not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate booking or overlapping session conflict")
    })
    public BookingResponse bookOffering(@Valid @RequestBody BookOfferingRequest request) {
        return parentBookingService.bookOffering(request);
    }

    @GetMapping("/{parentId}/bookings")
    @Operation(summary = "Get parent bookings", description = "Returns confirmed bookings with session times converted to the requested timezone or the parent's saved timezone.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings returned"),
            @ApiResponse(responseCode = "400", description = "Invalid timezone"),
            @ApiResponse(responseCode = "404", description = "Parent not found")
    })
    public List<BookingResponse> getBookings(
            @PathVariable UUID parentId,
            @Parameter(example = "Europe/London", description = "Optional response timezone override")
            @RequestParam(required = false) String timeZone
    ) {
        return parentBookingService.getBookings(parentId, timeZone);
    }
}
