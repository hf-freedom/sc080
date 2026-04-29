package com.university.coursesystem.controller;

import com.university.coursesystem.model.GraduationReview;
import com.university.coursesystem.service.GraduationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/graduation")
@CrossOrigin(origins = "http://localhost:3001")
public class GraduationController {
    
    @Autowired
    private GraduationService graduationService;
    
    @PostMapping("/review/{studentId}")
    public ResponseEntity<?> performReview(@PathVariable String studentId) {
        try {
            GraduationReview review = graduationService.performReview(studentId);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/review/{studentId}")
    public ResponseEntity<?> getLatestReview(@PathVariable String studentId,
                                              @RequestParam(defaultValue = "true") boolean checkArrears) {
        try {
            GraduationReview review = graduationService.getLatestReview(studentId, checkArrears);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            if (e.getMessage().contains("arrears")) {
                return ResponseEntity.status(403).body(error);
            }
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/required-courses")
    public List<String> getRequiredCourses() {
        return graduationService.getRequiredCourses();
    }
    
    @PutMapping("/required-courses")
    public void setRequiredCourses(@RequestBody List<String> courses) {
        graduationService.setRequiredCourses(courses);
    }
}
