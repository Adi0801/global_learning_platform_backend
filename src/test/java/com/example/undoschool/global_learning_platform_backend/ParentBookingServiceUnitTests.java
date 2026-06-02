package com.example.undoschool.global_learning_platform_backend;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.undoschool.global_learning_platform_backend.dto.request.BookOfferingRequest;
import com.example.undoschool.global_learning_platform_backend.entity.BookingStatus;
import com.example.undoschool.global_learning_platform_backend.entity.ClassSession;
import com.example.undoschool.global_learning_platform_backend.entity.Course;
import com.example.undoschool.global_learning_platform_backend.entity.Offering;
import com.example.undoschool.global_learning_platform_backend.entity.OfferingStatus;
import com.example.undoschool.global_learning_platform_backend.entity.ParentProfile;
import com.example.undoschool.global_learning_platform_backend.entity.Teacher;
import com.example.undoschool.global_learning_platform_backend.exception.BadRequestException;
import com.example.undoschool.global_learning_platform_backend.exception.BookingConflictException;
import com.example.undoschool.global_learning_platform_backend.repository.BookingRepository;
import com.example.undoschool.global_learning_platform_backend.repository.OfferingRepository;
import com.example.undoschool.global_learning_platform_backend.repository.ParentProfileRepository;
import com.example.undoschool.global_learning_platform_backend.service.OfferingMapper;
import com.example.undoschool.global_learning_platform_backend.service.ParentBookingService;
import com.example.undoschool.global_learning_platform_backend.service.TimeZoneService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParentBookingServiceUnitTests {

    private static final UUID PARENT_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID OFFERING_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private ParentProfileRepository parentProfileRepository;
    private OfferingRepository offeringRepository;
    private BookingRepository bookingRepository;
    private ParentBookingService parentBookingService;

    @BeforeEach
    void setUp() {
        parentProfileRepository = mock(ParentProfileRepository.class);
        offeringRepository = mock(OfferingRepository.class);
        bookingRepository = mock(BookingRepository.class);
        parentBookingService = new ParentBookingService(
                parentProfileRepository,
                offeringRepository,
                bookingRepository,
                new TimeZoneService(),
                mock(OfferingMapper.class)
        );
    }

    @Test
    void duplicateBookingFailsBeforeOverlapCheck() {
        ParentProfile parent = parent();
        Offering offering = offeringWithSession();
        when(parentProfileRepository.findByIdForUpdate(PARENT_ID)).thenReturn(Optional.of(parent));
        when(offeringRepository.findDetailedById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(bookingRepository.existsByParentIdAndOfferingIdAndStatus(PARENT_ID, OFFERING_ID, BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> parentBookingService.bookOffering(new BookOfferingRequest(PARENT_ID, OFFERING_ID)))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Parent has already booked this offering");
    }

    @Test
    void overlappingBookingFailsWithConflict() {
        ParentProfile parent = parent();
        Offering offering = offeringWithSession();
        when(parentProfileRepository.findByIdForUpdate(PARENT_ID)).thenReturn(Optional.of(parent));
        when(offeringRepository.findDetailedById(OFFERING_ID)).thenReturn(Optional.of(offering));
        when(bookingRepository.existsByParentIdAndOfferingIdAndStatus(PARENT_ID, OFFERING_ID, BookingStatus.CONFIRMED))
                .thenReturn(false);
        when(bookingRepository.hasOverlappingBooking(PARENT_ID, OFFERING_ID, BookingStatus.CONFIRMED))
                .thenReturn(true);

        assertThatThrownBy(() -> parentBookingService.bookOffering(new BookOfferingRequest(PARENT_ID, OFFERING_ID)))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Offering conflicts with one or more already booked sessions");

        verify(bookingRepository).hasOverlappingBooking(PARENT_ID, OFFERING_ID, BookingStatus.CONFIRMED);
    }

    @Test
    void offeringWithoutSessionsCannotBeBooked() {
        ParentProfile parent = parent();
        Offering offering = offeringBase();
        when(parentProfileRepository.findByIdForUpdate(PARENT_ID)).thenReturn(Optional.of(parent));
        when(offeringRepository.findDetailedById(OFFERING_ID)).thenReturn(Optional.of(offering));

        assertThatThrownBy(() -> parentBookingService.bookOffering(new BookOfferingRequest(PARENT_ID, OFFERING_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Offering must have at least one session before booking");
    }

    @Test
    void cancelledOfferingCannotBeBooked() {
        ParentProfile parent = parent();
        Offering offering = offeringWithSession();
        offering.setStatus(OfferingStatus.CANCELLED);
        when(parentProfileRepository.findByIdForUpdate(PARENT_ID)).thenReturn(Optional.of(parent));
        when(offeringRepository.findDetailedById(OFFERING_ID)).thenReturn(Optional.of(offering));

        assertThatThrownBy(() -> parentBookingService.bookOffering(new BookOfferingRequest(PARENT_ID, OFFERING_ID)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Offering is not available for booking");
    }

    private ParentProfile parent() {
        ParentProfile parent = new ParentProfile();
        parent.setId(PARENT_ID);
        parent.setName("Parent");
        parent.setEmail("parent@example.com");
        parent.setTimeZone("Asia/Kolkata");
        return parent;
    }

    private Offering offeringWithSession() {
        Offering offering = offeringBase();
        ClassSession session = new ClassSession();
        session.setId(UUID.randomUUID());
        session.setOffering(offering);
        session.setTeacher(offering.getTeacher());
        session.setStartAtUtc(Instant.parse("2026-06-13T12:30:00Z"));
        session.setEndAtUtc(Instant.parse("2026-06-13T13:30:00Z"));
        session.setSourceTimeZone("Asia/Kolkata");
        offering.getSessions().add(session);
        return offering;
    }

    private Offering offeringBase() {
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setTitle("Python Coding");

        Teacher teacher = new Teacher();
        teacher.setId(UUID.randomUUID());
        teacher.setName("Teacher");
        teacher.setEmail("teacher@example.com");
        teacher.setTimeZone("Asia/Kolkata");

        Offering offering = new Offering();
        offering.setId(OFFERING_ID);
        offering.setCourse(course);
        offering.setTeacher(teacher);
        offering.setTitle("Saturday Batch");
        offering.setTeacherTimeZone("Asia/Kolkata");
        offering.setStatus(OfferingStatus.PUBLISHED);
        return offering;
    }
}
