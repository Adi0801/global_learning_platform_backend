package com.example.undoschool.global_learning_platform_backend.service;

import com.example.undoschool.global_learning_platform_backend.dto.request.AddSessionRequest;
import com.example.undoschool.global_learning_platform_backend.dto.request.CreateOfferingRequest;
import com.example.undoschool.global_learning_platform_backend.dto.response.OfferingResponse;
import com.example.undoschool.global_learning_platform_backend.dto.response.SessionResponse;
import com.example.undoschool.global_learning_platform_backend.entity.ClassSession;
import com.example.undoschool.global_learning_platform_backend.entity.Course;
import com.example.undoschool.global_learning_platform_backend.entity.Offering;
import com.example.undoschool.global_learning_platform_backend.entity.Teacher;
import com.example.undoschool.global_learning_platform_backend.exception.BadRequestException;
import com.example.undoschool.global_learning_platform_backend.exception.ResourceNotFoundException;
import com.example.undoschool.global_learning_platform_backend.repository.ClassSessionRepository;
import com.example.undoschool.global_learning_platform_backend.repository.CourseRepository;
import com.example.undoschool.global_learning_platform_backend.repository.OfferingRepository;
import com.example.undoschool.global_learning_platform_backend.repository.TeacherRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherOfferingService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final OfferingRepository offeringRepository;
    private final ClassSessionRepository classSessionRepository;
    private final TimeZoneService timeZoneService;
    private final OfferingMapper offeringMapper;

    @Transactional
    public OfferingResponse createOffering(CreateOfferingRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.courseId()));
        Teacher teacher = teacherRepository.findById(request.teacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + request.teacherId()));
        ZoneId teacherZone = timeZoneService.requireValidZone(request.teacherTimeZone());

        Offering offering = new Offering();
        offering.setCourse(course);
        offering.setTeacher(teacher);
        offering.setTitle(request.title().trim());
        offering.setDescription(request.description());
        offering.setTeacherTimeZone(teacherZone.getId());

        return offeringMapper.toOfferingResponse(offeringRepository.save(offering), teacherZone);
    }

    @Transactional
    public SessionResponse addSession(UUID offeringId, AddSessionRequest request) {
        Offering offering = offeringRepository.findDetailedById(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException("Offering not found: " + offeringId));
        ZoneId sourceZone = timeZoneService.requireValidZone(
                request.timeZone() == null || request.timeZone().isBlank()
                        ? offering.getTeacherTimeZone()
                        : request.timeZone()
        );
        Instant startAtUtc = request.startDateTime().atZone(sourceZone).toInstant();
        Instant endAtUtc = request.endDateTime().atZone(sourceZone).toInstant();
        if (!endAtUtc.isAfter(startAtUtc)) {
            throw new BadRequestException("Session end time must be after start time");
        }

        ClassSession session = new ClassSession();
        session.setOffering(offering);
        session.setTeacher(offering.getTeacher());
        session.setStartAtUtc(startAtUtc);
        session.setEndAtUtc(endAtUtc);
        session.setSourceTimeZone(sourceZone.getId());

        ClassSession savedSession = classSessionRepository.save(session);
        return new SessionResponse(
                savedSession.getId(),
                offering.getId(),
                offering.getTeacher().getId(),
                savedSession.getStartAtUtc().atZone(sourceZone).toOffsetDateTime(),
                savedSession.getEndAtUtc().atZone(sourceZone).toOffsetDateTime(),
                sourceZone.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getTeacherOfferings(UUID teacherId) {
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher not found: " + teacherId);
        }
        return offeringRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(offering -> offeringMapper.toOfferingResponse(offering, ZoneId.of(offering.getTeacherTimeZone())))
                .toList();
    }
}
