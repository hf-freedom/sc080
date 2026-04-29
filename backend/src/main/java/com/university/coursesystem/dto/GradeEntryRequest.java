package com.university.coursesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeEntryRequest {
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Course ID is required")
    private String courseId;
    
    @NotNull(message = "Score is required")
    private Double score;
}
