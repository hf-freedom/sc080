package com.university.coursesystem.service;

import com.university.coursesystem.model.*;
import com.university.coursesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WithdrawService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private WaitlistRepository waitlistRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    public Enrollment withdrawStudent(String studentId, String courseId) {
        if (systemConfigService.isAfterWithdrawDeadline()) {
            throw new RuntimeException("Withdraw deadline has passed. Cannot withdraw from courses.");
        }
        
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (!enrollmentOpt.isPresent()) {
            throw new RuntimeException("Enrollment not found for student: " + studentId + " and course: " + courseId);
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new RuntimeException("Cannot withdraw. Current status: " + enrollment.getStatus());
        }
        
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment = enrollmentRepository.save(enrollment);
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        course.setEnrolledCount(course.getEnrolledCount() - 1);
        courseRepository.save(course);
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        student.getCurrentEnrollments().remove(courseId);
        studentRepository.save(student);
        
        processWaitlist(course);
        
        return enrollment;
    }
    
    private void processWaitlist(Course course) {
        List<WaitlistEntry> waitlist = waitlistRepository.findByCourseIdAndStatus(
                course.getCourseId(), WaitlistStatus.WAITING);
        
        if (waitlist.isEmpty()) {
            return;
        }
        
        while (course.getEnrolledCount() < course.getCapacity() && !waitlist.isEmpty()) {
            WaitlistEntry nextStudent = waitlist.remove(0);
            
            enrollFromWaitlist(nextStudent, course);
            
            waitlist = waitlistRepository.findByCourseIdAndStatus(
                    course.getCourseId(), WaitlistStatus.WAITING);
        }
    }
    
    private void enrollFromWaitlist(WaitlistEntry waitlistEntry, Course course) {
        Optional<Student> studentOpt = studentRepository.findById(waitlistEntry.getStudentId());
        if (!studentOpt.isPresent()) {
            waitlistEntry.setStatus(WaitlistStatus.CANCELLED);
            waitlistRepository.save(waitlistEntry);
            waitlistRepository.reorderPositions(course.getCourseId());
            return;
        }
        
        Student student = studentOpt.get();
        
        if (student.getStatus() != StudentStatus.ENROLLED) {
            waitlistEntry.setStatus(WaitlistStatus.CANCELLED);
            waitlistRepository.save(waitlistEntry);
            waitlistRepository.reorderPositions(course.getCourseId());
            return;
        }
        
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getStudentId(), course.getCourseId())) {
            waitlistEntry.setStatus(WaitlistStatus.CANCELLED);
            waitlistRepository.save(waitlistEntry);
            waitlistRepository.reorderPositions(course.getCourseId());
            return;
        }
        
        try {
            Enrollment enrollment = Enrollment.builder()
                    .studentId(student.getStudentId())
                    .courseId(course.getCourseId())
                    .enrolledAt(LocalDateTime.now())
                    .status(EnrollmentStatus.ENROLLED)
                    .build();
            
            enrollmentRepository.save(enrollment);
            
            course.setEnrolledCount(course.getEnrolledCount() + 1);
            courseRepository.save(course);
            
            student.getCurrentEnrollments().add(course.getCourseId());
            studentRepository.save(student);
            
            waitlistEntry.setStatus(WaitlistStatus.ENROLLED);
            waitlistRepository.save(waitlistEntry);
            
            waitlistRepository.reorderPositions(course.getCourseId());
            
        } catch (Exception e) {
            waitlistEntry.setStatus(WaitlistStatus.CANCELLED);
            waitlistRepository.save(waitlistEntry);
            waitlistRepository.reorderPositions(course.getCourseId());
        }
    }
    
    public boolean cancelWaitlist(String studentId, String courseId) {
        Optional<WaitlistEntry> entryOpt = waitlistRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (!entryOpt.isPresent()) {
            throw new RuntimeException("Waitlist entry not found");
        }
        
        WaitlistEntry entry = entryOpt.get();
        if (entry.getStatus() != WaitlistStatus.WAITING) {
            throw new RuntimeException("Cannot cancel. Current status: " + entry.getStatus());
        }
        
        entry.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(entry);
        waitlistRepository.reorderPositions(courseId);
        
        return true;
    }
    
    public boolean canWithdraw() {
        return !systemConfigService.isAfterWithdrawDeadline();
    }
}
