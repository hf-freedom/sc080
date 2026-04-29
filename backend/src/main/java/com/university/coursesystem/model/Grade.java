package com.university.coursesystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    private String gradeId;
    private String studentId;
    private String courseId;
    private Double score;
    private String letterGrade;
    private Double gradePoints;
    private Boolean creditsEarned;
    private LocalDateTime enteredAt;
    private LocalDateTime updatedAt;
    private GradeStatus status;
}
