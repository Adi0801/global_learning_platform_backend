package com.example.undoschool.global_learning_platform_backend.controller;

import com.example.undoschool.global_learning_platform_backend.dto.request.AddSessionRequest;
import com.example.undoschool.global_learning_platform_backend.dto.request.CreateOfferingRequest;
import com.example.undoschool.global_learning_platform_backend.dto.response.OfferingResponse;
import com.example.undoschool.global_learning_platform_backend.dto.response.SessionResponse;
import com.example.undoschool.global_learning_platform_backend.service.TeacherOfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teacher")
@Tag(name = "Teacher Offerings", description = "APIs for teachers to manage course offerings and sessions.")
public class TeacherOfferingController {

    private final TeacherOfferingService teacherOfferingService;

    @PostMapping("/offerings")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create offering", description = "Creates a schedulable course offering for a teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Offering created",
                    content = @Content(schema = @Schema(implementation = OfferingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Course or teacher not found")
    })
    public OfferingResponse createOffering(@Valid @RequestBody CreateOfferingRequest request) {
        return teacherOfferingService.createOffering(request);
    }

    @PostMapping("/offerings/{offeringId}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add session", description = "Adds one class session to an existing offering. Times are accepted in the request timezone or the offering teacher timezone.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Session added",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid time range or timezone"),
            @ApiResponse(responseCode = "404", description = "Offering not found")
    })
    public SessionResponse addSession(
            @PathVariable UUID offeringId,
            @Valid @RequestBody AddSessionRequest request
    ) {
        return teacherOfferingService.addSession(offeringId, request);
    }

    @GetMapping("/{teacherId}/offerings")
    @Operation(summary = "Get teacher offerings", description = "Returns all offerings and sessions owned by a teacher in the teacher's timezone.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offerings returned"),
            @ApiResponse(responseCode = "404", description = "Teacher not found")
    })
    public List<OfferingResponse> getTeacherOfferings(@PathVariable UUID teacherId) {
        return teacherOfferingService.getTeacherOfferings(teacherId);
    }
}
