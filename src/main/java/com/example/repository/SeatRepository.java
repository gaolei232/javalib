package com.example.repository;

import com.example.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByStatus(String status);

    @Query("SELECT s FROM Seat s WHERE s.row = :row AND s.col = :col")
    List<Seat> findByRowAndCol(@Param("row") String row, @Param("col") Integer col);

    Seat findBySeatId(String seatId);

    boolean existsBySeatId(String seatId);

    List<Seat> findByBuildingCodeAndFloorNoOrderByRowAscColAsc(String buildingCode, Integer floorNo);

    @Query("SELECT s FROM Seat s WHERE s.status = :status ORDER BY s.row, s.col")
    List<Seat> findByStatusOrderByRowCol(@Param("status") String status);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT s FROM Seat s WHERE s.lastBookedBy = :userId")
    List<Seat> findByLastBookedBy(@Param("userId") String userId);

    @Query("UPDATE Seat s SET s.status = :newStatus WHERE s.seatId = :seatId")
    void updateSeatStatus(@Param("seatId") String seatId, @Param("newStatus") String newStatus);
}
