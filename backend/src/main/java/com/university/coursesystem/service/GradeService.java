package com.university.coursesystem.service;

import com.university.coursesystem.model.*;
import com.university.coursesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GradeService {
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    @Autowired
    private MakeupExamRepository makeupExamRepository;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Autowired
    private GraduationService graduationService;
    
    public Grade enterGrade(String studentId, String courseId, Double score) {
        if (score < 0 || score > 100) {
            throw new RuntimeException("Score must be between 0 and 100");
        }
        
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (!enrollmentOpt.isPresent()) {
            throw new RuntimeException("Enrollment not found for student: " + studentId + " and course: " + courseId);
        }
        
        Enrollment enrollment = enrollmentOpt.get();
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new RuntimeException("Cannot enter grade. Current enrollment status: " + enrollment.getStatus());
        }
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        
        double gradePoints = systemConfigService.getGradePoints(score);
        String letterGrade = systemConfigService.getLetterGrade(score);
        boolean creditsEarned = score >= systemConfigService.getPassingScore();
        
        Optional<Grade> existingGradeOpt = gradeRepository.findByStudentIdAndCourseId(studentId, courseId);
        Grade grade;
        
        if (existingGradeOpt.isPresent()) {
            grade = existingGradeOpt.get();
            grade.setScore(score);
            grade.setLetterGrade(letterGrade);
            grade.setGradePoints(gradePoints);
            grade.setCreditsEarned(creditsEarned);
            grade.setUpdatedAt(LocalDateTime.now());
            grade.setStatus(GradeStatus.FINAL);
        } else {
            grade = Grade.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .score(score)
                    .letterGrade(letterGrade)
                    .gradePoints(gradePoints)
                    .creditsEarned(creditsEarned)
                    .enteredAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status(GradeStatus.FINAL)
                    .build();
        }
        
        grade = gradeRepository.save(grade);
        
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(enrollment);
        
        updateStudentCreditsAndGpa(studentId, course, grade);
        
        if (!creditsEarned) {
            createMakeupExamEligibility(studentId, courseId);
        }
        
        graduationService.triggerReview(studentId);
        
        return grade;
    }
    
    private void updateStudentCreditsAndGpa(String studentId, Course course, Grade grade) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        List<Grade> allGrades = gradeRepository.findByStudentId(studentId);
        
        double totalCredits = 0.0;
        double weightedGradePoints = 0.0;
        double creditsForGpa = 0.0;
        
        for (Grade g : allGrades) {
            Course c = courseRepository.findById(g.getCourseId()).orElse(null);
            if (c == null) continue;
            
            if (g.getCreditsEarned()) {
                totalCredits += c.getCredits();
            }
            
            if (g.getStatus() == GradeStatus.FINAL || g.getStatus() == GradeStatus.MAKEUP) {
                weightedGradePoints += g.getGradePoints() * c.getCredits();
                creditsForGpa += c.getCredits();
            }
        }
        
        student.setTotalCredits(totalCredits);
        
        if (creditsForGpa > 0) {
            double gpa = weightedGradePoints / creditsForGpa;
            student.setGpa(Math.round(gpa * 100.0) / 100.0);
        } else {
            student.setGpa(0.0);
        }
        
        if (grade.getCreditsEarned() && !student.getCompletedCourses().contains(course.getCourseId())) {
            student.getCompletedCourses().add(course.getCourseId());
        }
        
        student.getCurrentEnrollments().remove(course.getCourseId());
        
        studentRepository.save(student);
    }
    
    private void createMakeupExamEligibility(String studentId, String courseId) {
        if (makeupExamRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            return;
        }
        
        MakeupExam exam = MakeupExam.builder()
                .studentId(studentId)
                .courseId(courseId)
                .createdAt(LocalDateTime.now())
                .status(MakeupExamStatus.ELIGIBLE)
                .build();
        
        makeupExamRepository.save(exam);
    }
    
    public MakeupExam enterMakeupScore(String studentId, String courseId, Double score) {
        if (score < 0 || score > 100) {
            throw new RuntimeException("Score must be between 0 and 100");
        }
        
        Optional<MakeupExam> examOpt = makeupExamRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (!examOpt.isPresent()) {
            throw new RuntimeException("Makeup exam not found for student: " + studentId + " and course: " + courseId);
        }
        
        MakeupExam exam = examOpt.get();
        if (exam.getStatus() != MakeupExamStatus.ELIGIBLE && exam.getStatus() != MakeupExamStatus.SCHEDULED) {
            throw new RuntimeException("Cannot enter makeup score. Current status: " + exam.getStatus());
        }
        
        boolean passed = score >= systemConfigService.getPassingScore();
        String letterGrade = systemConfigService.getLetterGrade(score);
        
        exam.setScore(score);
        exam.setLetterGrade(letterGrade);
        exam.setPassed(passed);
        exam.setTakenAt(LocalDateTime.now());
        exam.setStatus(passed ? MakeupExamStatus.PASSED : MakeupExamStatus.FAILED);
        
        exam = makeupExamRepository.save(exam);
        
        if (passed) {
            updateStudentForMakeupPass(studentId, courseId, score);
        }
        
        graduationService.triggerReview(studentId);
        
        return exam;
    }
    
    private void updateStudentForMakeupPass(String studentId, String courseId, Double score) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
        
        Optional<Grade> gradeOpt = gradeRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (gradeOpt.isPresent()) {
            Grade grade = gradeOpt.get();
            grade.setScore(Math.max(score, 60.0));
            grade.setLetterGrade(systemConfigService.getLetterGrade(60.0));
            grade.setGradePoints(systemConfigService.getGradePoints(60.0));
            grade.setCreditsEarned(true);
            grade.setStatus(GradeStatus.MAKEUP);
            grade.setUpdatedAt(LocalDateTime.now());
            gradeRepository.save(grade);
        } else {
            double gradePoints = systemConfigService.getGradePoints(60.0);
            Grade grade = Grade.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .score(60.0)
                    .letterGrade("D")
                    .gradePoints(gradePoints)
                    .creditsEarned(true)
                    .enteredAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status(GradeStatus.MAKEUP)
                    .build();
            gradeRepository.save(grade);
        }
        
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));
        
        if (!student.getCompletedCourses().contains(courseId)) {
            student.getCompletedCourses().add(courseId);
        }
        
        double newTotal = student.getTotalCredits() + course.getCredits();
        student.setTotalCredits(newTotal);
        
        studentRepository.save(student);
    }
    
    public List<Grade> getStudentGrades(String studentId) {
        return gradeRepository.findByStudentId(studentId);
    }
    
    public List<MakeupExam> getStudentMakeupExams(String studentId) {
        return makeupExamRepository.findByStudentId(studentId);
    }
    
    public List<MakeupExam> getEligibleMakeupExams() {
        return makeupExamRepository.findByStatus(MakeupExamStatus.ELIGIBLE);
    }
    
    public Grade completeCourse(String studentId, String courseId) {
        return enterGrade(studentId, courseId, 85.0);
    }
    
    public Grade completeCourseWithScore(String studentId, String courseId, Double score) {
        return enterGrade(studentId, courseId, score);
    }
}
