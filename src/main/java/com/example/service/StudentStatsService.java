package com.example.service;

import com.example.entity.SeatBooking;
import com.example.entity.StudentStats;
import com.example.entity.User;
import com.example.repository.SeatBookingRepository;
import com.example.repository.StudentStatsRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentStatsService {

    @Autowired
    private StudentStatsRepository studentStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatBookingRepository seatBookingRepository;

    public List<StudentStats> getAllStudentStats() {
        List<StudentStats> stats = studentStatsRepository.findAll();
        if (stats.isEmpty()) {
            rebuildStatsFromBookings();
            stats = studentStatsRepository.findAll();
        }
        return stats;
    }

    public StudentStats getStudentStatsById(Long id) {
        return studentStatsRepository.findById(id).orElse(null);
    }

    public StudentStats getStudentStatsByStudentId(Long studentId) {
        return studentStatsRepository.findByStudentId(studentId).orElse(null);
    }

    public List<StudentStats> getStudentStatsByName(String name) {
        return studentStatsRepository.findByStudentNameContaining(name);
    }

    public List<StudentStats> getTopStudentsByBookings() {
        List<StudentStats> stats = studentStatsRepository.findAllByOrderByTotalBookingsDesc();
        if (stats.isEmpty()) {
            rebuildStatsFromBookings();
            stats = studentStatsRepository.findAllByOrderByTotalBookingsDesc();
        }
        return stats;
    }

    public StudentStats createStudentStats(StudentStats stats) {
        return studentStatsRepository.save(stats);
    }

    public StudentStats updateStudentStats(Long id, StudentStats stats) {
        StudentStats existingStats = studentStatsRepository.findById(id).orElse(null);
        if (existingStats != null) {
            existingStats.setStudentId(stats.getStudentId());
            existingStats.setStudentName(stats.getStudentName());
            existingStats.setTotalBookings(stats.getTotalBookings());
            existingStats.setCompletedBookings(stats.getCompletedBookings());
            existingStats.setCancelledBookings(stats.getCancelledBookings());
            existingStats.setTotalHours(stats.getTotalHours());
            existingStats.setLastBookingDate(stats.getLastBookingDate());
            return studentStatsRepository.save(existingStats);
        }
        return null;
    }

    public boolean deleteStudentStats(Long id) {
        if (studentStatsRepository.existsById(id)) {
            studentStatsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public StudentStats incrementBookingCount(Long studentId, String studentName, int hours) {
        Optional<StudentStats> statsOpt = studentStatsRepository.findByStudentId(studentId);
        if (statsOpt.isPresent()) {
            StudentStats stats = statsOpt.get();
            stats.setTotalBookings(stats.getTotalBookings() + 1);
            stats.setCompletedBookings(stats.getCompletedBookings() + 1);
            stats.setTotalHours(stats.getTotalHours() + hours);
            stats.setLastBookingDate(LocalDate.now());
            return studentStatsRepository.save(stats);
        } else {
            StudentStats newStats = new StudentStats();
            newStats.setStudentId(studentId);
            newStats.setStudentName(studentName);
            newStats.setTotalBookings(1);
            newStats.setCompletedBookings(1);
            newStats.setCancelledBookings(0);
            newStats.setTotalHours(hours);
            newStats.setLastBookingDate(LocalDate.now());
            return studentStatsRepository.save(newStats);
        }
    }

    @Transactional
    public StudentStats incrementCancelledCount(Long studentId, String studentName) {
        Optional<StudentStats> statsOpt = studentStatsRepository.findByStudentId(studentId);
        if (statsOpt.isPresent()) {
            StudentStats stats = statsOpt.get();
            stats.setTotalBookings(stats.getTotalBookings() + 1);
            stats.setCancelledBookings(stats.getCancelledBookings() + 1);
            return studentStatsRepository.save(stats);
        } else {
            StudentStats newStats = new StudentStats();
            newStats.setStudentId(studentId);
            newStats.setStudentName(studentName);
            newStats.setTotalBookings(1);
            newStats.setCompletedBookings(0);
            newStats.setCancelledBookings(1);
            newStats.setTotalHours(0);
            newStats.setLastBookingDate(LocalDate.now());
            return studentStatsRepository.save(newStats);
        }
    }

    @Transactional
    public void rebuildStatsFromBookings() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                continue;
            }

            Long studentId = user.getId();
            String studentName = user.getName();

            List<SeatBooking> allBookings = seatBookingRepository.findAll();
            int total = 0;
            int completed = 0;
            int cancelled = 0;
            LocalDate lastDate = null;

            for (SeatBooking booking : allBookings) {
                if (!String.valueOf(studentId).equals(booking.getUserId())) {
                    continue;
                }

                total++;
                if ("BOOKED".equals(booking.getStatus())) {
                    completed++;
                } else if ("CANCELLED".equals(booking.getStatus())) {
                    cancelled++;
                }

                if (booking.getBookingDate() != null && (lastDate == null || booking.getBookingDate().isAfter(lastDate))) {
                    lastDate = booking.getBookingDate();
                }
            }

            Optional<StudentStats> exists = studentStatsRepository.findByStudentId(studentId);
            StudentStats stats = exists.orElseGet(StudentStats::new);
            stats.setStudentId(studentId);
            stats.setStudentName(studentName);
            stats.setTotalBookings(total);
            stats.setCompletedBookings(completed);
            stats.setCancelledBookings(cancelled);
            stats.setTotalHours(completed * 3);
            stats.setLastBookingDate(lastDate);
            studentStatsRepository.save(stats);
        }
    }

    public Map<String, Object> getSystemStatistics() {
        List<User> users = userRepository.findAll();
        long totalStudents = users.stream().filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole())).count();

        List<SeatBooking> allBookings = seatBookingRepository.findAll();
        long totalBookings = allBookings.size();
        long completedBookings = allBookings.stream().filter(b -> "BOOKED".equals(b.getStatus())).count();
        long cancelledBookings = allBookings.stream().filter(b -> "CANCELLED".equals(b.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("totalBookings", totalBookings);
        stats.put("completedBookings", completedBookings);
        stats.put("cancelledBookings", cancelledBookings);
        stats.put("avgHours", totalStudents == 0 ? 0 : Math.round((completedBookings * 3.0 / totalStudents) * 10.0) / 10.0);
        return stats;
    }

    public Long getTotalStudents() {
        return (long) userRepository.findAll().stream().filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole())).count();
    }

    public Long getTotalBookings() {
        return (long) seatBookingRepository.findAll().size();
    }
}
