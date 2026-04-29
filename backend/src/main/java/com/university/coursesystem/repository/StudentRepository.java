package com.university.coursesystem.repository;

import com.university.coursesystem.model.Student;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class StudentRepository {
    private final Map<String, Student> students = new HashMap<>();
    
    public Student save(Student student) {
        students.put(student.getStudentId(), student);
        return student;
    }
    
    public Optional<Student> findById(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }
    
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }
    
    public boolean existsById(String studentId) {
        return students.containsKey(studentId);
    }
    
    public void deleteById(String studentId) {
        students.remove(studentId);
    }
}
