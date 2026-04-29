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
public class Course {
    private String courseId;
    private String courseName;
    private Integer credits;
    private CourseType type;
    private Integer capacity;
    private Integer enrolledCount;
    private String instructor;
    private List<TimeSlot> schedule;
    private List<String> prerequisites;
}
