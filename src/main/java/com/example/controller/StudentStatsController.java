package com.example.controller;

import com.example.entity.StudentStats;
import com.example.service.StudentStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student-stats")
@CrossOrigin(origins = "*")
public class StudentStatsController {

    @Autowired
    private StudentStatsService studentStatsService;

    @GetMapping
    public ResponseEntity<List<StudentStats>> getAllStudentStats() {
        List<StudentStats> stats = studentStatsService.getAllStudentStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentStats> getStudentStatsById(@PathVariable Long id) {
        StudentStats stats = studentStatsService.getStudentStatsById(id);
        if (stats != null) {
            return ResponseEntity.ok(stats);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentStats> getStudentStatsByStudentId(@PathVariable Long studentId) {
        StudentStats stats = studentStatsService.getStudentStatsByStudentId(studentId);
        if (stats != null) {
            return ResponseEntity.ok(stats);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<StudentStats>> getStudentStatsByName(@PathVariable String name) {
        List<StudentStats> stats = studentStatsService.getStudentStatsByName(name);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top")
    public ResponseEntity<List<StudentStats>> getTopStudents() {
        List<StudentStats> stats = studentStatsService.getTopStudentsByBookings();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        Map<String, Object> stats = studentStatsService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/total-students")
    public ResponseEntity<Long> getTotalStudents() {
        Long count = studentStatsService.getTotalStudents();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-bookings")
    public ResponseEntity<Long> getTotalBookings() {
        Long count = studentStatsService.getTotalBookings();
        return ResponseEntity.ok(count);
    }

    @PostMapping
    public ResponseEntity<StudentStats> createStudentStats(@RequestBody StudentStats stats) {
        StudentStats createdStats = studentStatsService.createStudentStats(stats);
        return ResponseEntity.ok(createdStats);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentStats> updateStudentStats(@PathVariable Long id, @RequestBody StudentStats stats) {
        StudentStats updatedStats = studentStatsService.updateStudentStats(id, stats);
        if (updatedStats != null) {
            return ResponseEntity.ok(updatedStats);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/booking")
    public ResponseEntity<Map<String, Object>> recordBooking(
            @RequestParam Long studentId,
            @RequestParam String studentName,
            @RequestParam Integer hours) {
        StudentStats stats = studentStatsService.incrementBookingCount(studentId, studentName, hours);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "预约记录已更新");
        response.put("stats", stats);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancellation")
    public ResponseEntity<Map<String, Object>> recordCancellation(
            @RequestParam Long studentId,
            @RequestParam String studentName) {
        StudentStats stats = studentStatsService.incrementCancelledCount(studentId, studentName);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "取消记录已更新");
        response.put("stats", stats);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentStats(@PathVariable Long id) {
        boolean deleted = studentStatsService.deleteStudentStats(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}