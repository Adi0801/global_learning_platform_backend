package com.example.undoschool.global_learning_platform_backend.repository;

import com.example.undoschool.global_learning_platform_backend.entity.Teacher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
}
