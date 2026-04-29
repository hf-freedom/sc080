package com.university.coursesystem.repository;

import com.university.coursesystem.model.MakeupExam;
import com.university.coursesystem.model.MakeupExamStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MakeupExamRepository {
    private final Map<String, MakeupExam> exams = new HashMap<>();
    private long nextId = 1;
    
    public MakeupExam save(MakeupExam exam) {
        if (exam.getExamId() == null) {
            exam.setExamId("ME" + nextId++);
        }
        exams.put(exam.getExamId(), exam);
        return exam;
    }
    
    public Optional<MakeupExam> findById(String examId) {
        return Optional.ofNullable(exams.get(examId));
    }
    
    public List<MakeupExam> findAll() {
        return new ArrayList<>(exams.values());
    }
    
    public List<MakeupExam> findByStudentId(String studentId) {
        return exams.values().stream()
                .filter(e -> studentId.equals(e.getStudentId()))
                .collect(Collectors.toList());
    }
    
    public List<MakeupExam> findByCourseId(String courseId) {
        return exams.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .collect(Collectors.toList());
    }
    
    public Optional<MakeupExam> findByStudentIdAndCourseId(String studentId, String courseId) {
        return exams.values().stream()
                .filter(e -> studentId.equals(e.getStudentId()) && courseId.equals(e.getCourseId()))
                .findFirst();
    }
    
    public List<MakeupExam> findByStatus(MakeupExamStatus status) {
        return exams.values().stream()
                .filter(e -> status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }
    
    public boolean existsByStudentIdAndCourseId(String studentId, String courseId) {
        return exams.values().stream()
                .anyMatch(e -> studentId.equals(e.getStudentId()) && courseId.equals(e.getCourseId()));
    }
    
    public void deleteById(String examId) {
        exams.remove(examId);
    }
}
