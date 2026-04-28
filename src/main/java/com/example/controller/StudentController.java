package com.example.controller;

import com.example.entity.StudentStats;
import com.example.service.StudentStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentStatsService studentStatsService;

    @GetMapping
    public List<StudentStats> getAllStudents() {
        return studentStatsService.getAllStudentStats();
    }

    @GetMapping("/{id}")
    public StudentStats getStudentById(@PathVariable Long id) {
        return studentStatsService.getStudentStatsById(id);
    }

    @GetMapping("/student-id/{studentId}")
    public StudentStats getStudentByStudentId(@PathVariable Long studentId) {
        return studentStatsService.getStudentStatsByStudentId(studentId);
    }

    @PostMapping
    public StudentStats createStudent(@RequestBody StudentStats studentStats) {
        return studentStatsService.createStudentStats(studentStats);
    }

    @PutMapping("/{id}")
    public StudentStats updateStudent(@PathVariable Long id, @RequestBody StudentStats studentStats) {
        return studentStatsService.updateStudentStats(id, studentStats);
    }

    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable Long id) {
        studentStatsService.deleteStudentStats(id);
    }

}
