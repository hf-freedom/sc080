package com.university.coursesystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollRequest {
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Course ID is required")
    private String courseId;
}
