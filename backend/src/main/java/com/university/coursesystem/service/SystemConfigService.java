package com.university.coursesystem.service;

import com.university.coursesystem.model.CourseType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class SystemConfigService {
    
    private final Map<String, Object> configs = new HashMap<>();
    
    public SystemConfigService() {
        initializeDefaults();
    }
    
    private void initializeDefaults() {
        configs.put("withdrawDeadline", LocalDate.now().plusDays(14));
        configs.put("passingScore", 60.0);
        configs.put("minimumGpa", 2.0);
        configs.put("totalCreditsRequired", 140.0);
        configs.put("majorRequiredCreditsRequired", 80.0);
        configs.put("electiveCreditsRequired", 30.0);
        configs.put("generalEdCreditsRequired", 30.0);
    }
    
    public LocalDate getWithdrawDeadline() {
        return (LocalDate) configs.getOrDefault("withdrawDeadline", LocalDate.now().plusDays(14));
    }
    
    public void setWithdrawDeadline(LocalDate deadline) {
        configs.put("withdrawDeadline", deadline);
    }
    
    public boolean isAfterWithdrawDeadline() {
        return LocalDate.now().isAfter(getWithdrawDeadline());
    }
    
    public Double getPassingScore() {
        return (Double) configs.getOrDefault("passingScore", 60.0);
    }
    
    public void setPassingScore(Double score) {
        configs.put("passingScore", score);
    }
    
    public Double getMinimumGpa() {
        return (Double) configs.getOrDefault("minimumGpa", 2.0);
    }
    
    public void setMinimumGpa(Double gpa) {
        configs.put("minimumGpa", gpa);
    }
    
    public Double getTotalCreditsRequired() {
        return (Double) configs.getOrDefault("totalCreditsRequired", 140.0);
    }
    
    public Double getCreditsRequiredByType(CourseType type) {
        switch (type) {
            case REQUIRED:
                return (Double) configs.getOrDefault("majorRequiredCreditsRequired", 80.0);
            case ELECTIVE:
                return (Double) configs.getOrDefault("electiveCreditsRequired", 30.0);
            case GENERAL_EDUCATION:
                return (Double) configs.getOrDefault("generalEdCreditsRequired", 30.0);
            default:
                return 0.0;
        }
    }
    
    public double getGradePoints(double score) {
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 66) return 1.7;
        if (score >= 64) return 1.3;
        if (score >= 60) return 1.0;
        return 0.0;
    }
    
    public String getLetterGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 85) return "A-";
        if (score >= 82) return "B+";
        if (score >= 78) return "B";
        if (score >= 75) return "B-";
        if (score >= 72) return "C+";
        if (score >= 68) return "C";
        if (score >= 66) return "C-";
        if (score >= 64) return "D+";
        if (score >= 60) return "D";
        return "F";
    }
}
