package com.university.coursesystem.controller;

import com.university.coursesystem.dto.GradeEntryRequest;
import com.university.coursesystem.model.Grade;
import com.university.coursesystem.model.MakeupExam;
import com.university.coursesystem.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "http://localhost:3001")
public class GradeController {
    
    @Autowired
    private GradeService gradeService;
    
    @PostMapping("/enter")
    public ResponseEntity<?> enterGrade(@Valid @RequestBody GradeEntryRequest request) {
        try {
            Grade grade = gradeService.enterGrade(
                    request.getStudentId(), request.getCourseId(), request.getScore());
            return ResponseEntity.ok(grade);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/makeup")
    public ResponseEntity<?> enterMakeupGrade(@Valid @RequestBody GradeEntryRequest request) {
        try {
            MakeupExam exam = gradeService.enterMakeupScore(
                    request.getStudentId(), request.getCourseId(), request.getScore());
            return ResponseEntity.ok(exam);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/student/{studentId}")
    public List<Grade> getStudentGrades(@PathVariable String studentId) {
        return gradeService.getStudentGrades(studentId);
    }
    
    @GetMapping("/student/{studentId}/makeup-exams")
    public List<MakeupExam> getStudentMakeupExams(@PathVariable String studentId) {
        return gradeService.getStudentMakeupExams(studentId);
    }
    
    @GetMapping("/makeup-exams/eligible")
    public List<MakeupExam> getEligibleMakeupExams() {
        return gradeService.getEligibleMakeupExams();
    }
    
    @PostMapping("/complete")
    public ResponseEntity<?> completeCourse(@Valid @RequestBody GradeEntryRequest request) {
        try {
            Double score = request.getScore() != null ? request.getScore() : 85.0;
            Grade grade = gradeService.completeCourseWithScore(
                    request.getStudentId(), request.getCourseId(), score);
            return ResponseEntity.ok(grade);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/complete/{studentId}/{courseId}")
    public ResponseEntity<?> completeCourseDefaultScore(
            @PathVariable String studentId,
            @PathVariable String courseId) {
        try {
            Grade grade = gradeService.completeCourse(studentId, courseId);
            return ResponseEntity.ok(grade);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
