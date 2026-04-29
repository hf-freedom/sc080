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
public class Enrollment {
    private String enrollmentId;
    private String studentId;
    private String courseId;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus status;
}
