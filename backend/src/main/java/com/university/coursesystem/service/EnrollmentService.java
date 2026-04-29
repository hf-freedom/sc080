package com.university.coursesystem.service;

import com.university.coursesystem.model.*;
import com.university.coursesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private WaitlistRepository waitlistRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    public Enrollment enrollStudent(String studentId, String courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        
        if (student.getStatus() != StudentStatus.ENROLLED) {
            throw new RuntimeException("Student is not enrolled. Status: " + student.getStatus());
        }
        
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        if (waitlistRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("Student is already in waitlist for this course");
        }
        
        validatePrerequisites(student, course);
        
        checkTimeConflict(student, course);
        
        if (course.getEnrolledCount() >= course.getCapacity()) {
            addToWaitlist(student, course);
            throw new RuntimeException("Course is full. Added to waitlist.");
        }
        
        return createEnrollment(student, course);
    }
    
    private void validatePrerequisites(Student student, Course course) {
        List<String> prerequisites = course.getPrerequisites();
        if (prerequisites == null || prerequisites.isEmpty()) {
            return;
        }
        
        List<String> completedCourses = student.getCompletedCourses();
        for (String prereq : prerequisites) {
            if (!completedCourses.contains(prereq)) {
                Course prereqCourse = courseRepository.findById(prereq).orElse(null);
                String prereqName = prereqCourse != null ? prereqCourse.getCourseName() : prereq;
                throw new RuntimeException("Prerequisite not completed: " + prereqName);
            }
        }
    }
    
    private void checkTimeConflict(Student student, Course newCourse) {
        List<Enrollment> currentEnrollments = enrollmentRepository.findByStudentIdAndStatus(
                student.getStudentId(), EnrollmentStatus.ENROLLED);
        
        List<TimeSlot> newSchedule = newCourse.getSchedule();
        if (newSchedule == null || newSchedule.isEmpty()) {
            return;
        }
        
        for (Enrollment enrollment : currentEnrollments) {
            Course enrolledCourse = courseRepository.findById(enrollment.getCourseId()).orElse(null);
            if (enrolledCourse == null) continue;
            
            List<TimeSlot> enrolledSchedule = enrolledCourse.getSchedule();
            if (enrolledSchedule == null) continue;
            
            for (TimeSlot newSlot : newSchedule) {
                for (TimeSlot enrolledSlot : enrolledSchedule) {
                    if (hasTimeConflict(newSlot, enrolledSlot)) {
                        throw new RuntimeException(String.format(
                                "Time conflict detected with course: %s. " +
                                "New course time: %s %02d:%02d-%02d:%02d conflicts with " +
                                "enrolled course: %s %s %02d:%02d-%02d:%02d",
                                newCourse.getCourseName(),
                                newSlot.getDay(), newSlot.getStartHour(), newSlot.getStartMinute(),
                                newSlot.getEndHour(), newSlot.getEndMinute(),
                                enrolledCourse.getCourseName(),
                                enrolledSlot.getDay(), enrolledSlot.getStartHour(), enrolledSlot.getStartMinute(),
                                enrolledSlot.getEndHour(), enrolledSlot.getEndMinute()
                        ));
                    }
                }
            }
        }
    }
    
    private boolean hasTimeConflict(TimeSlot slot1, TimeSlot slot2) {
        if (slot1.getDay() != slot2.getDay()) {
            return false;
        }
        
        int start1 = slot1.getStartHour() * 60 + slot1.getStartMinute();
        int end1 = slot1.getEndHour() * 60 + slot1.getEndMinute();
        int start2 = slot2.getStartHour() * 60 + slot2.getStartMinute();
        int end2 = slot2.getEndHour() * 60 + slot2.getEndMinute();
        
        return !(end1 <= start2 || end2 <= start1);
    }
    
    private Enrollment createEnrollment(Student student, Course course) {
        Enrollment enrollment = Enrollment.builder()
                .studentId(student.getStudentId())
                .courseId(course.getCourseId())
                .enrolledAt(LocalDateTime.now())
                .status(EnrollmentStatus.ENROLLED)
                .build();
        
        enrollment = enrollmentRepository.save(enrollment);
        
        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);
        
        student.getCurrentEnrollments().add(course.getCourseId());
        studentRepository.save(student);
        
        return enrollment;
    }
    
    private WaitlistEntry addToWaitlist(Student student, Course course) {
        int maxPosition = waitlistRepository.getMaxPositionByCourseId(course.getCourseId());
        
        WaitlistEntry entry = WaitlistEntry.builder()
                .studentId(student.getStudentId())
                .courseId(course.getCourseId())
                .position(maxPosition + 1)
                .waitlistedAt(LocalDateTime.now())
                .status(WaitlistStatus.WAITING)
                .build();
        
        return waitlistRepository.save(entry);
    }
    
    public List<WaitlistEntry> getWaitlistByCourse(String courseId) {
        return waitlistRepository.findByCourseIdAndStatus(courseId, WaitlistStatus.WAITING);
    }
    
    public Optional<WaitlistEntry> getWaitlistPosition(String studentId, String courseId) {
        return waitlistRepository.findByStudentIdAndCourseId(studentId, courseId);
    }
}
