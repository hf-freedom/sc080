package com.university.coursesystem.service;

import com.university.coursesystem.model.*;
import com.university.coursesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GraduationService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private GraduationReviewRepository reviewRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    private final List<String> requiredCourses = new ArrayList<>();
    
    public GraduationService() {
        requiredCourses.add("CS101");
        requiredCourses.add("CS102");
        requiredCourses.add("CS201");
        requiredCourses.add("CS202");
        requiredCourses.add("CS301");
        requiredCourses.add("CS401");
    }
    
    public GraduationReview performReview(String studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<String> failureReasons = new ArrayList<>();
        List<String> missingRequiredCourses = new ArrayList<>();
        
        double majorRequiredCreditsEarned = 0.0;
        double electiveCreditsEarned = 0.0;
        double generalEdCreditsEarned = 0.0;
        
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        List<String> completedCourses = new ArrayList<>();
        
        for (Grade grade : grades) {
            if (!grade.getCreditsEarned()) continue;
            
            Course course = courseRepository.findById(grade.getCourseId()).orElse(null);
            if (course == null) continue;
            
            completedCourses.add(course.getCourseId());
            
            switch (course.getType()) {
                case REQUIRED:
                    majorRequiredCreditsEarned += course.getCredits();
                    break;
                case ELECTIVE:
                    electiveCreditsEarned += course.getCredits();
                    break;
                case GENERAL_EDUCATION:
                    generalEdCreditsEarned += course.getCredits();
                    break;
            }
        }
        
        for (String requiredCourse : requiredCourses) {
            if (!completedCourses.contains(requiredCourse)) {
                missingRequiredCourses.add(requiredCourse);
            }
        }
        
        double totalCreditsEarned = majorRequiredCreditsEarned + electiveCreditsEarned + generalEdCreditsEarned;
        double majorRequiredCreditsRequired = systemConfigService.getCreditsRequiredByType(CourseType.REQUIRED);
        double minimumGpa = systemConfigService.getMinimumGpa();
        
        if (student.getHasArrears()) {
            failureReasons.add("Student has outstanding arrears");
        }
        
        if (!missingRequiredCourses.isEmpty()) {
            failureReasons.add("Missing required courses: " + String.join(", ", missingRequiredCourses));
        }
        
        if (majorRequiredCreditsEarned < majorRequiredCreditsRequired) {
            failureReasons.add(String.format(
                    "Insufficient major required credits: %.1f/%.1f",
                    majorRequiredCreditsEarned, majorRequiredCreditsRequired));
        }
        
        if (student.getGpa() == null || student.getGpa() < minimumGpa) {
            double currentGpa = student.getGpa() != null ? student.getGpa() : 0.0;
            failureReasons.add(String.format(
                    "GPA below minimum: %.2f/%.2f", currentGpa, minimumGpa));
        }
        
        boolean eligible = failureReasons.isEmpty();
        
        GraduationReview review = GraduationReview.builder()
                .studentId(studentId)
                .eligible(eligible)
                .totalCredits(totalCreditsEarned)
                .majorRequiredCreditsRequired(majorRequiredCreditsRequired)
                .majorRequiredCreditsEarned(majorRequiredCreditsEarned)
                .electiveCreditsEarned(electiveCreditsEarned)
                .generalEdCreditsEarned(generalEdCreditsEarned)
                .gpa(student.getGpa() != null ? student.getGpa() : 0.0)
                .minimumGpa(minimumGpa)
                .hasArrears(student.getHasArrears())
                .missingRequiredCourses(missingRequiredCourses)
                .failureReasons(failureReasons)
                .reviewedAt(LocalDateTime.now())
                .status(determineStatus(student, eligible))
                .build();
        
        return reviewRepository.save(review);
    }
    
    private GraduationReviewStatus determineStatus(Student student, boolean eligible) {
        if (student.getHasArrears()) {
            return GraduationReviewStatus.ARREARS_BLOCKED;
        }
        return eligible ? GraduationReviewStatus.ELIGIBLE : GraduationReviewStatus.NOT_ELIGIBLE;
    }
    
    public GraduationReview getLatestReview(String studentId, boolean checkArrears) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        if (checkArrears && student.getHasArrears()) {
            throw new RuntimeException("Cannot view graduation review: Student has outstanding arrears");
        }
        
        return reviewRepository.findLatestByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("No graduation review found for student: " + studentId));
    }
    
    public void triggerReview(String studentId) {
        performReview(studentId);
    }
    
    public void setRequiredCourses(List<String> courses) {
        this.requiredCourses.clear();
        this.requiredCourses.addAll(courses);
    }
    
    public List<String> getRequiredCourses() {
        return new ArrayList<>(this.requiredCourses);
    }
}
