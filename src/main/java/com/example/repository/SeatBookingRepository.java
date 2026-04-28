package com.example.repository;

import com.example.entity.SeatBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatBookingRepository extends JpaRepository<SeatBooking, Long> {

    List<SeatBooking> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);

    List<SeatBooking> findByStatusOrderByCreatedAtDesc(String status);

    List<SeatBooking> findAllByOrderByCreatedAtDesc();

    Optional<SeatBooking> findByIdAndStatus(Long id, String status);

    List<SeatBooking> findBySeatIdAndBookingDateAndStatusOrderByStartTimeAsc(
            String seatId,
            LocalDate bookingDate,
            String status
    );

    List<SeatBooking> findByBookingDateAndStatusAndEndTimeLessThanEqual(
            LocalDate bookingDate,
            String status,
            LocalTime endTime
    );

    Optional<SeatBooking> findBySeatIdAndBookingDateAndStartTimeAndEndTimeAndStatus(
            String seatId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime,
            String status
    );

    boolean existsBySeatIdAndBookingDateAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            String seatId,
            LocalDate bookingDate,
            String status,
            LocalTime endTime,
            LocalTime startTime
    );

    Long countByStatus(String status);
}