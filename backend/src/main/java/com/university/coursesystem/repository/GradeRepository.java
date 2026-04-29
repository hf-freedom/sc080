package com.university.coursesystem.repository;

import com.university.coursesystem.model.Grade;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GradeRepository {
    private final Map<String, Grade> grades = new HashMap<>();
    private long nextId = 1;
    
    public Grade save(Grade grade) {
        if (grade.getGradeId() == null) {
            grade.setGradeId("GRADE" + nextId++);
        }
        grades.put(grade.getGradeId(), grade);
        return grade;
    }
    
    public Optional<Grade> findById(String gradeId) {
        return Optional.ofNullable(grades.get(gradeId));
    }
    
    public List<Grade> findAll() {
        return new ArrayList<>(grades.values());
    }
    
    public List<Grade> findByStudentId(String studentId) {
        return grades.values().stream()
                .filter(g -> studentId.equals(g.getStudentId()))
                .collect(Collectors.toList());
    }
    
    public List<Grade> findByCourseId(String courseId) {
        return grades.values().stream()
                .filter(g -> courseId.equals(g.getCourseId()))
                .collect(Collectors.toList());
    }
    
    public Optional<Grade> findByStudentIdAndCourseId(String studentId, String courseId) {
        return grades.values().stream()
                .filter(g -> studentId.equals(g.getStudentId()) && courseId.equals(g.getCourseId()))
                .findFirst();
    }
    
    public boolean existsByStudentIdAndCourseId(String studentId, String courseId) {
        return grades.values().stream()
                .anyMatch(g -> studentId.equals(g.getStudentId()) && courseId.equals(g.getCourseId()));
    }
    
    public void deleteById(String gradeId) {
        grades.remove(gradeId);
    }
}
