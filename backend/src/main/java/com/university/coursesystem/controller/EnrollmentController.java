package com.university.coursesystem.controller;

import com.university.coursesystem.dto.EnrollRequest;
import com.university.coursesystem.model.Enrollment;
import com.university.coursesystem.model.WaitlistEntry;
import com.university.coursesystem.repository.EnrollmentRepository;
import com.university.coursesystem.service.EnrollmentService;
import com.university.coursesystem.service.WithdrawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "http://localhost:3001")
public class EnrollmentController {
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private WithdrawService withdrawService;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@Valid @RequestBody EnrollRequest request) {
        try {
            Enrollment enrollment = enrollmentService.enrollStudent(
                    request.getStudentId(), request.getCourseId());
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("waitlist")) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "waitlisted");
                response.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            }
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawStudent(@Valid @RequestBody EnrollRequest request) {
        try {
            Enrollment enrollment = withdrawService.withdrawStudent(
                    request.getStudentId(), request.getCourseId());
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/student/{studentId}")
    public List<Enrollment> getStudentEnrollments(@PathVariable String studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }
    
    @GetMapping("/course/{courseId}")
    public List<Enrollment> getCourseEnrollments(@PathVariable String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }
    
    @GetMapping("/waitlist/course/{courseId}")
    public List<WaitlistEntry> getCourseWaitlist(@PathVariable String courseId) {
        return enrollmentService.getWaitlistByCourse(courseId);
    }
    
    @PostMapping("/waitlist/cancel")
    public ResponseEntity<?> cancelWaitlist(@Valid @RequestBody EnrollRequest request) {
        try {
            boolean cancelled = withdrawService.cancelWaitlist(
                    request.getStudentId(), request.getCourseId());
            Map<String, Object> response = new HashMap<>();
            response.put("cancelled", cancelled);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/withdraw/status")
    public Map<String, Object> getWithdrawStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("canWithdraw", withdrawService.canWithdraw());
        return status;
    }
}
