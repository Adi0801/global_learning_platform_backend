package com.example.undoschool.global_learning_platform_backend.service;

import com.example.undoschool.global_learning_platform_backend.dto.request.BookOfferingRequest;
import com.example.undoschool.global_learning_platform_backend.dto.response.BookingResponse;
import com.example.undoschool.global_learning_platform_backend.dto.response.OfferingResponse;
import com.example.undoschool.global_learning_platform_backend.entity.Booking;
import com.example.undoschool.global_learning_platform_backend.entity.BookingStatus;
import com.example.undoschool.global_learning_platform_backend.entity.Offering;
import com.example.undoschool.global_learning_platform_backend.entity.OfferingStatus;
import com.example.undoschool.global_learning_platform_backend.entity.ParentProfile;
import com.example.undoschool.global_learning_platform_backend.exception.BadRequestException;
import com.example.undoschool.global_learning_platform_backend.exception.BookingConflictException;
import com.example.undoschool.global_learning_platform_backend.exception.ResourceNotFoundException;
import com.example.undoschool.global_learning_platform_backend.repository.BookingRepository;
import com.example.undoschool.global_learning_platform_backend.repository.OfferingRepository;
import com.example.undoschool.global_learning_platform_backend.repository.ParentProfileRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentBookingService {

    private final ParentProfileRepository parentProfileRepository;
    private final OfferingRepository offeringRepository;
    private final BookingRepository bookingRepository;
    private final TimeZoneService timeZoneService;
    private final OfferingMapper offeringMapper;

    @Transactional(readOnly = true)
    public List<OfferingResponse> getAvailableOfferings(String timeZone) {
        ZoneId responseZone = timeZoneService.requireValidZone(timeZone);
        return offeringRepository.findAvailableOfferings(OfferingStatus.PUBLISHED, Instant.now()).stream()
                .map(offering -> offeringMapper.toOfferingResponse(offering, responseZone))
                .toList();
    }

    @Transactional
    public BookingResponse bookOffering(BookOfferingRequest request) {
        ParentProfile parent = parentProfileRepository.findByIdForUpdate(request.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + request.parentId()));
        Offering offering = offeringRepository.findDetailedById(request.offeringId())
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found: " + request.offeringId()));

        if (offering.getStatus() != OfferingStatus.PUBLISHED) {
            throw new BadRequestException("Offering is not available for booking");
        }
        if (offering.getSessions().isEmpty()) {
            throw new BadRequestException("Offering must have at least one session before booking");
        }
        if (bookingRepository.existsByParentIdAndOfferingIdAndStatus(
                parent.getId(), offering.getId(), BookingStatus.CONFIRMED)) {
            throw new BookingConflictException("Parent has already booked this offering");
        }
        if (bookingRepository.hasOverlappingBooking(parent.getId(), offering.getId(), BookingStatus.CONFIRMED)) {
            throw new BookingConflictException("Offering conflicts with one or more already booked sessions");
        }

        Booking booking = new Booking();
        booking.setParent(parent);
        booking.setOffering(offering);
        Booking savedBooking = bookingRepository.save(booking);
        Booking detailedBooking = bookingRepository.findDetailedById(savedBooking.getId()).orElse(savedBooking);

        return offeringMapper.toBookingResponse(detailedBooking, ZoneId.of(parent.getTimeZone()));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookings(UUID parentId, String timeZone) {
        ParentProfile parent = parentProfileRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found: " + parentId));
        ZoneId responseZone = timeZone == null || timeZone.isBlank()
                ? ZoneId.of(parent.getTimeZone())
                : timeZoneService.requireValidZone(timeZone);
        return bookingRepository.findByParentIdAndStatusOrderByBookedAtDesc(parentId, BookingStatus.CONFIRMED).stream()
                .map(booking -> offeringMapper.toBookingResponse(booking, responseZone))
                .toList();
    }
}
