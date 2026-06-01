package com.example.undoschool.global_learning_platform_backend.service;

import com.example.undoschool.global_learning_platform_backend.dto.response.BookingResponse;
import com.example.undoschool.global_learning_platform_backend.dto.response.OfferingResponse;
import com.example.undoschool.global_learning_platform_backend.dto.response.SessionResponse;
import com.example.undoschool.global_learning_platform_backend.entity.Booking;
import com.example.undoschool.global_learning_platform_backend.entity.ClassSession;
import com.example.undoschool.global_learning_platform_backend.entity.Offering;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class OfferingMapper {

    public OfferingResponse toOfferingResponse(Offering offering, ZoneId responseZone) {
        return new OfferingResponse(
                offering.getId(),
                offering.getCourse().getId(),
                offering.getCourse().getTitle(),
                offering.getTeacher().getId(),
                offering.getTeacher().getName(),
                offering.getTitle(),
                offering.getDescription(),
                offering.getTeacherTimeZone(),
                offering.getStatus(),
                offering.getSessions().stream()
                        .map(session -> toSessionResponse(session, responseZone))
                        .toList()
        );
    }

    public BookingResponse toBookingResponse(Booking booking, ZoneId responseZone) {
        return new BookingResponse(
                booking.getId(),
                booking.getParent().getId(),
                booking.getOffering().getId(),
                booking.getStatus(),
                booking.getBookedAt().atZone(responseZone).toOffsetDateTime(),
                toOfferingResponse(booking.getOffering(), responseZone)
        );
    }

    private SessionResponse toSessionResponse(ClassSession session, ZoneId responseZone) {
        return new SessionResponse(
                session.getId(),
                session.getOffering().getId(),
                session.getTeacher().getId(),
                session.getStartAtUtc().atZone(responseZone).toOffsetDateTime(),
                session.getEndAtUtc().atZone(responseZone).toOffsetDateTime(),
                responseZone.getId()
        );
    }
}
