package com.example.controller;

import com.example.entity.Seat;
import com.example.entity.SeatBooking;
import com.example.entity.SystemConfig;
import com.example.repository.SystemConfigRepository;
import com.example.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "*")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @GetMapping
    public ResponseEntity<List<Seat>> getAllSeats(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timeSlotId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String buildingCode,
            @RequestParam(required = false) Integer floorNo
    ) {
        if (date != null && !date.isBlank()) {
            List<Seat> seats = seatService.getSeatsByDateAndTimeSlot(
                    LocalDate.parse(date),
                    timeSlotId,
                    userId,
                    startTime,
                    buildingCode,
                    floorNo
            );
            return ResponseEntity.ok(seats);
        }

        List<Seat> seats = seatService.getAllSeats(buildingCode, floorNo);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<SeatBooking>> getAllBookings(
            @RequestParam(required = false) String status
    ) {
        if ("BOOKED".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(seatService.getAllActiveBookings());
        }
        return ResponseEntity.ok(seatService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seat> getSeatById(@PathVariable Long id) {
        Seat seat = seatService.getSeatById(id);
        if (seat != null) {
            return ResponseEntity.ok(seat);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/seat-id/{seatId}")
    public ResponseEntity<Seat> getSeatBySeatId(@PathVariable String seatId) {
        Seat seat = seatService.getSeatBySeatId(seatId);
        if (seat != null) {
            return ResponseEntity.ok(seat);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Seat>> getSeatsByStatus(@PathVariable String status) {
        List<Seat> seats = seatService.getSeatsByStatus(status);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Seat>> getAvailableSeats() {
        List<Seat> seats = seatService.getAvailableSeats();
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/row/{row}/col/{col}")
    public ResponseEntity<List<Seat>> getSeatsByRowAndCol(@PathVariable String row, @PathVariable Integer col) {
        List<Seat> seats = seatService.getSeatsByRowAndCol(row, col);
        return ResponseEntity.ok(seats);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSeatStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Long available = seatService.getAvailableSeatCount();
        Long booked = seatService.getBookedSeatCount();
        Long used = seatService.getUsedSeatCount();

        stats.put("available", available);
        stats.put("booked", booked);
        stats.put("used", used);
        stats.put("total", available + booked + used);
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    public ResponseEntity<Seat> createSeat(@RequestBody Seat seat) {
        Seat createdSeat = seatService.createSeat(seat);
        return ResponseEntity.ok(createdSeat);
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeSeats() {
        seatService.initializeSeats();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "座位初始化成功");
        response.put("totalSeats", seatService.getAllSeats().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/randomize-attributes")
    public ResponseEntity<Map<String, Object>> randomizeSeatAttributes() {
        int updated = seatService.randomizeSeatAttributes();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "座位属性已随机化");
        response.put("updatedSeats", updated);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Seat> updateSeat(@PathVariable Long id, @RequestBody Seat seat) {
        Seat updatedSeat = seatService.updateSeat(id, seat);
        if (updatedSeat != null) {
            return ResponseEntity.ok(updatedSeat);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{seatId}/book")
    public ResponseEntity<Map<String, Object>> bookSeat(
            @PathVariable String seatId,
            @RequestParam String userId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalDate bookingDate = LocalDate.parse(date);
            LocalTime parsedStartTime = LocalTime.parse(startTime);
            LocalTime parsedEndTime = LocalTime.parse(endTime);

            // 诚信指数检查
            Map<String, Object> integrity = seatService.getUserIntegrity(Long.parseLong(userId));
            SystemConfig thresholdConfig = systemConfigRepository.findByConfigKey("integrity_threshold");
            double threshold = thresholdConfig != null ? Double.parseDouble(thresholdConfig.getConfigValue()) : 0.6;
            double userScore = ((Number) integrity.get("integrityScore")).doubleValue();

            if (userScore < threshold) {
                response.put("success", false);
                response.put("message", "您的诚信指数过低（" + Math.round(userScore * 100) + "%），无法预约，诚信阈值：" + Math.round(threshold * 100) + "%");
                return ResponseEntity.ok(response);
            }

            boolean success = seatService.bookSeatWithDuration(
                    seatId,
                    userId,
                    bookingDate,
                    parsedStartTime,
                    parsedEndTime
            );

            if (success) {
                response.put("success", true);
                response.put("message", "搴т綅棰勭害鎴愬姛");
                response.put("seatId", seatId);
                response.put("date", date);
                response.put("startTime", startTime);
                response.put("endTime", endTime);
            } else {
                response.put("success", false);
                response.put("message", "璇ユ椂娈靛骇浣嶅凡琚绾︽垨璇锋眰鍙傛暟鏃犳晥");
            }

            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "鏃ユ湡鎴栨椂闂存牸寮忛敊璇紝姝ｇ‘鏍煎紡濡傦細date=2026-03-12锛宻tartTime=09:00锛宔ndTime=10:30");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 鎺ㄨ崘鍙栨秷鏂瑰紡锛氭寜 bookingId 鍙栨秷
     * 璋冪敤绀轰緥锛?     * POST /api/seats/{seatId}/cancel?bookingId=123
     */
    @PostMapping("/{seatId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable String seatId,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        Map<String, Object> response = new HashMap<>();
        boolean success = false;

        try {
            if (bookingId != null) {
                success = seatService.cancelBooking(bookingId);
            } else if (date != null && startTime != null && endTime != null) {
                success = seatService.cancelBooking(
                        seatId,
                        LocalDate.parse(date),
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime)
                );
            } else {
                response.put("success", false);
                response.put("message", "缺少取消预约所需参数，请提供 bookingId 或 date + startTime + endTime");
                return ResponseEntity.badRequest().body(response);
            }

            if (success) {
                response.put("success", true);
                response.put("message", "预约已取消");
            } else {
                response.put("success", false);
                response.put("message", "取消预约失败，未找到有效预约记录");
            }

            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "日期或时间格式错误");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/checkin/{bookingId}")
    public ResponseEntity<Map<String, Object>> checkin(
            @PathVariable Long bookingId,
            @RequestParam String userId) {
        Map<String, Object> response = seatService.checkin(bookingId, userId);
        boolean success = Boolean.TRUE.equals(response.get("success"));
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/{seatId}/reset")
    public ResponseEntity<Map<String, Object>> resetSeat(@PathVariable String seatId) {
        boolean success = seatService.resetSeat(seatId);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "座位已重置");
        } else {
            response.put("success", false);
            response.put("message", "重置失败");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/bookings")
    public ResponseEntity<List<SeatBooking>> getUserBookedSeats(@PathVariable String userId) {
        List<SeatBooking> bookings = seatService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{seatId}/bookings")
    public ResponseEntity<List<SeatBooking>> getSeatBookingsByDate(
            @PathVariable String seatId,
            @RequestParam String date
    ) {
        try {
            List<SeatBooking> bookings = seatService.getBookingsBySeatAndDate(
                    seatId,
                    LocalDate.parse(date)
            );
            return ResponseEntity.ok(bookings);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        boolean deleted = seatService.deleteSeat(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

