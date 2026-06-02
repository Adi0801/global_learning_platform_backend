package com.example.undoschool.global_learning_platform_backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.undoschool.global_learning_platform_backend.dto.request.BookOfferingRequest;
import com.example.undoschool.global_learning_platform_backend.dto.response.BookingResponse;
import com.example.undoschool.global_learning_platform_backend.entity.BookingStatus;
import com.example.undoschool.global_learning_platform_backend.exception.BookingConflictException;
import com.example.undoschool.global_learning_platform_backend.repository.BookingRepository;
import com.example.undoschool.global_learning_platform_backend.service.ParentBookingService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ParentBookingServiceIntegrationTests {

    private static final UUID PARENT_WITH_BOOKING = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID OTHER_PARENT = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID BOOKED_OFFERING = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID OVERLAPPING_OFFERING = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID NON_OVERLAPPING_OFFERING = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @Autowired
    private ParentBookingService parentBookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void duplicateOfferingBookingReturnsConflict() {
        BookOfferingRequest request = new BookOfferingRequest(PARENT_WITH_BOOKING, BOOKED_OFFERING);

        assertThatThrownBy(() -> parentBookingService.bookOffering(request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Parent has already booked this offering");
    }

    @Test
    void overlappingOfferingBookingReturnsConflict() {
        BookOfferingRequest request = new BookOfferingRequest(PARENT_WITH_BOOKING, OVERLAPPING_OFFERING);

        assertThat(bookingRepository.hasOverlappingBooking(
                PARENT_WITH_BOOKING,
                OVERLAPPING_OFFERING,
                BookingStatus.CONFIRMED
        )).isTrue();

        assertThatThrownBy(() -> parentBookingService.bookOffering(request))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Offering conflicts with one or more already booked sessions");
    }

    @Test
    void nonOverlappingOfferingBookingSucceeds() {
        BookOfferingRequest request = new BookOfferingRequest(OTHER_PARENT, NON_OVERLAPPING_OFFERING);

        BookingResponse response = parentBookingService.bookOffering(request);

        assertThat(response.parentId()).isEqualTo(OTHER_PARENT);
        assertThat(response.offeringId()).isEqualTo(NON_OVERLAPPING_OFFERING);
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.offering().sessions()).hasSize(1);
        assertThat(response.offering().sessions().get(0).timeZone()).isEqualTo("Europe/London");
        assertThat(response.offering().sessions().get(0).startTime().toString()).isEqualTo("2026-06-09T22:00+01:00");
    }
}
