package com.university.coursesystem.repository;

import com.university.coursesystem.model.Course;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CourseRepository {
    private final Map<String, Course> courses = new HashMap<>();
    
    public Course save(Course course) {
        courses.put(course.getCourseId(), course);
        return course;
    }
    
    public Optional<Course> findById(String courseId) {
        return Optional.ofNullable(courses.get(courseId));
    }
    
    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }
    
    public boolean existsById(String courseId) {
        return courses.containsKey(courseId);
    }
    
    public void deleteById(String courseId) {
        courses.remove(courseId);
    }
}
