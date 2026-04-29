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
public class WaitlistEntry {
    private String waitlistId;
    private String studentId;
    private String courseId;
    private Integer position;
    private LocalDateTime waitlistedAt;
    private WaitlistStatus status;
}
