package com.university.coursesystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private String studentId;
    private String name;
    private String major;
    private Integer grade;
    private StudentStatus status;
    private Double totalCredits;
    private Double gpa;
    private Boolean hasArrears;
    
    @Builder.Default
    private List<String> completedCourses = new ArrayList<>();
    
    @Builder.Default
    private List<String> currentEnrollments = new ArrayList<>();
}
