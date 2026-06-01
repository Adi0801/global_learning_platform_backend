package com.example.undoschool.global_learning_platform_backend.repository;

import com.example.undoschool.global_learning_platform_backend.entity.Course;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {
}
