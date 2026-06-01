package com.example.undoschool.global_learning_platform_backend.repository;

import com.example.undoschool.global_learning_platform_backend.entity.ParentProfile;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParentProfileRepository extends JpaRepository<ParentProfile, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ParentProfile p where p.id = :id")
    Optional<ParentProfile> findByIdForUpdate(@Param("id") UUID id);
}
