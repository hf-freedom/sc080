package com.university.coursesystem.repository;

import com.university.coursesystem.model.GraduationReview;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class GraduationReviewRepository {
    private final Map<String, GraduationReview> reviews = new HashMap<>();
    private long nextId = 1;
    
    public GraduationReview save(GraduationReview review) {
        if (review.getReviewId() == null) {
            review.setReviewId("GR" + nextId++);
        }
        reviews.put(review.getReviewId(), review);
        return review;
    }
    
    public Optional<GraduationReview> findById(String reviewId) {
        return Optional.ofNullable(reviews.get(reviewId));
    }
    
    public List<GraduationReview> findAll() {
        return new ArrayList<>(reviews.values());
    }
    
    public List<GraduationReview> findByStudentId(String studentId) {
        return reviews.values().stream()
                .filter(r -> studentId.equals(r.getStudentId()))
                .collect(Collectors.toList());
    }
    
    public Optional<GraduationReview> findLatestByStudentId(String studentId) {
        return reviews.values().stream()
                .filter(r -> studentId.equals(r.getStudentId()))
                .sorted((r1, r2) -> r2.getReviewedAt().compareTo(r1.getReviewedAt()))
                .findFirst();
    }
    
    public void deleteById(String reviewId) {
        reviews.remove(reviewId);
    }
}
