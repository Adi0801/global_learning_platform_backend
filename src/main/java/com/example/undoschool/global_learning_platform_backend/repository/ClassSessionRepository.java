package com.example.undoschool.global_learning_platform_backend.repository;

import com.example.undoschool.global_learning_platform_backend.entity.ClassSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {
}
