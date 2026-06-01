package com.example.undoschool.global_learning_platform_backend.repository;

import com.example.undoschool.global_learning_platform_backend.entity.Booking;
import com.example.undoschool.global_learning_platform_backend.entity.BookingStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    boolean existsByParentIdAndOfferingIdAndStatus(UUID parentId, UUID offeringId, BookingStatus status);

    @EntityGraph(attributePaths = {"parent", "offering", "offering.course", "offering.teacher", "offering.sessions"})
    List<Booking> findByParentIdAndStatusOrderByBookedAtDesc(UUID parentId, BookingStatus status);

    @EntityGraph(attributePaths = {"parent", "offering", "offering.course", "offering.teacher", "offering.sessions"})
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findDetailedById(@Param("id") UUID id);

    @Query("""
            select count(candidateSession) > 0
            from ClassSession candidateSession
            where candidateSession.offering.id = :offeringId
              and exists (
                  select 1
                  from Booking booking
                  join booking.offering bookedOffering
                  join bookedOffering.sessions bookedSession
                  where booking.parent.id = :parentId
                    and booking.status = :status
                    and bookedSession.startAtUtc < candidateSession.endAtUtc
                    and bookedSession.endAtUtc > candidateSession.startAtUtc
              )
            """)
    boolean hasOverlappingBooking(
            @Param("parentId") UUID parentId,
            @Param("offeringId") UUID offeringId,
            @Param("status") BookingStatus status
    );
}
