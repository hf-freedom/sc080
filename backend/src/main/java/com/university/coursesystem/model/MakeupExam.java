package com.university.coursesystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeupExam {
    private String examId;
    private String studentId;
    private String courseId;
    private LocalDate examDate;
    private Double score;
    private String letterGrade;
    private Boolean passed;
    private LocalDateTime createdAt;
    private LocalDateTime takenAt;
    private MakeupExamStatus status;
}
