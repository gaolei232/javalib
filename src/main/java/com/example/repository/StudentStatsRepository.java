package com.example.repository;

import com.example.entity.StudentStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentStatsRepository extends JpaRepository<StudentStats, Long> {

    Optional<StudentStats> findByStudentId(Long studentId);

    List<StudentStats> findAllByOrderByTotalBookingsDesc();

    @Query("SELECT s FROM StudentStats s WHERE s.studentName LIKE %:name%")
    List<StudentStats> findByStudentNameContaining(@Param("name") String name);

    @Query("SELECT COUNT(s) FROM StudentStats s")
    Long countTotalStudents();

    @Query("SELECT SUM(s.totalBookings) FROM StudentStats s")
    Long sumTotalBookings();

    @Query("SELECT SUM(s.completedBookings) FROM StudentStats s")
    Long sumCompletedBookings();

    @Query("SELECT SUM(s.cancelledBookings) FROM StudentStats s")
    Long sumCancelledBookings();

    @Query("SELECT AVG(s.totalHours) FROM StudentStats s")
    Double avgTotalHours();
}