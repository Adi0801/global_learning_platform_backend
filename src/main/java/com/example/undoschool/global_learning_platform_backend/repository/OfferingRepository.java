package com.example.undoschool.global_learning_platform_backend.repository;

import com.example.undoschool.global_learning_platform_backend.entity.Offering;
import com.example.undoschool.global_learning_platform_backend.entity.OfferingStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferingRepository extends JpaRepository<Offering, UUID> {

    @EntityGraph(attributePaths = {"course", "teacher", "sessions"})
    List<Offering> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    @EntityGraph(attributePaths = {"course", "teacher", "sessions"})
    @Query("""
            select distinct o
            from Offering o
            join o.sessions s
            where o.status = :status
              and s.endAtUtc > :from
            order by o.createdAt desc
            """)
    List<Offering> findAvailableOfferings(@Param("status") OfferingStatus status, @Param("from") Instant from);

    @EntityGraph(attributePaths = {"course", "teacher", "sessions"})
    @Query("select o from Offering o where o.id = :id")
    java.util.Optional<Offering> findDetailedById(@Param("id") UUID id);
}
