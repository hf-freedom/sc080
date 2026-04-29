package com.university.coursesystem.repository;

import com.university.coursesystem.model.Enrollment;
import com.university.coursesystem.model.EnrollmentStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EnrollmentRepository {
    private final Map<String, Enrollment> enrollments = new HashMap<>();
    private long nextId = 1;
    
    public Enrollment save(Enrollment enrollment) {
        if (enrollment.getEnrollmentId() == null) {
            enrollment.setEnrollmentId("ENR" + nextId++);
        }
        enrollments.put(enrollment.getEnrollmentId(), enrollment);
        return enrollment;
    }
    
    public Optional<Enrollment> findById(String enrollmentId) {
        return Optional.ofNullable(enrollments.get(enrollmentId));
    }
    
    public List<Enrollment> findAll() {
        return new ArrayList<>(enrollments.values());
    }
    
    public List<Enrollment> findByStudentId(String studentId) {
        return enrollments.values().stream()
                .filter(e -> studentId.equals(e.getStudentId()))
                .collect(Collectors.toList());
    }
    
    public List<Enrollment> findByCourseId(String courseId) {
        return enrollments.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .collect(Collectors.toList());
    }
    
    public Optional<Enrollment> findByStudentIdAndCourseId(String studentId, String courseId) {
        return enrollments.values().stream()
                .filter(e -> studentId.equals(e.getStudentId()) && courseId.equals(e.getCourseId()))
                .findFirst();
    }
    
    public List<Enrollment> findByStudentIdAndStatus(String studentId, EnrollmentStatus status) {
        return enrollments.values().stream()
                .filter(e -> studentId.equals(e.getStudentId()) && status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }
    
    public List<Enrollment> findByCourseIdAndStatus(String courseId, EnrollmentStatus status) {
        return enrollments.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()) && status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }
    
    public boolean existsByStudentIdAndCourseId(String studentId, String courseId) {
        return enrollments.values().stream()
                .anyMatch(e -> studentId.equals(e.getStudentId()) && courseId.equals(e.getCourseId()));
    }
    
    public void deleteById(String enrollmentId) {
        enrollments.remove(enrollmentId);
    }
}
