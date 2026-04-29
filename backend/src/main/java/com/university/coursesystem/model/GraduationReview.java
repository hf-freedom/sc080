package com.university.coursesystem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraduationReview {
    private String reviewId;
    private String studentId;
    private Boolean eligible;
    private Double totalCredits;
    private Double majorRequiredCreditsRequired;
    private Double majorRequiredCreditsEarned;
    private Double electiveCreditsEarned;
    private Double generalEdCreditsEarned;
    private Double gpa;
    private Double minimumGpa;
    private Boolean hasArrears;
    private List<String> missingRequiredCourses;
    private List<String> failureReasons;
    private LocalDateTime reviewedAt;
    private GraduationReviewStatus status;
}
