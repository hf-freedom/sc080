package com.university.coursesystem.repository;

import com.university.coursesystem.model.WaitlistEntry;
import com.university.coursesystem.model.WaitlistStatus;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class WaitlistRepository {
    private final Map<String, WaitlistEntry> waitlistEntries = new HashMap<>();
    private long nextId = 1;
    
    public WaitlistEntry save(WaitlistEntry entry) {
        if (entry.getWaitlistId() == null) {
            entry.setWaitlistId("WL" + nextId++);
        }
        waitlistEntries.put(entry.getWaitlistId(), entry);
        return entry;
    }
    
    public Optional<WaitlistEntry> findById(String waitlistId) {
        return Optional.ofNullable(waitlistEntries.get(waitlistId));
    }
    
    public List<WaitlistEntry> findAll() {
        return new ArrayList<>(waitlistEntries.values());
    }
    
    public List<WaitlistEntry> findByCourseId(String courseId) {
        return waitlistEntries.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .collect(Collectors.toList());
    }
    
    public List<WaitlistEntry> findByCourseIdAndStatus(String courseId, WaitlistStatus status) {
        return waitlistEntries.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()) && status.equals(e.getStatus()))
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .collect(Collectors.toList());
    }
    
    public Optional<WaitlistEntry> findByStudentIdAndCourseId(String studentId, String courseId) {
        return waitlistEntries.values().stream()
                .filter(e -> studentId.equals(e.getStudentId()) && courseId.equals(e.getCourseId()))
                .findFirst();
    }
    
    public boolean existsByStudentIdAndCourseId(String studentId, String courseId) {
        return waitlistEntries.values().stream()
                .anyMatch(e -> studentId.equals(e.getStudentId()) && courseId.equals(e.getCourseId()));
    }
    
    public int getMaxPositionByCourseId(String courseId) {
        return waitlistEntries.values().stream()
                .filter(e -> courseId.equals(e.getCourseId()))
                .mapToInt(WaitlistEntry::getPosition)
                .max()
                .orElse(0);
    }
    
    public void deleteById(String waitlistId) {
        waitlistEntries.remove(waitlistId);
    }
    
    public void reorderPositions(String courseId) {
        List<WaitlistEntry> entries = findByCourseIdAndStatus(courseId, WaitlistStatus.WAITING);
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setPosition(i + 1);
            save(entries.get(i));
        }
    }
}
