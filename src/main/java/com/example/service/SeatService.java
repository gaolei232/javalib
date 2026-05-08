package com.example.service;

import com.example.entity.Seat;
import com.example.entity.SeatBooking;
import com.example.repository.SeatBookingRepository;
import com.example.repository.SeatRepository;
import com.example.websocket.SeatStatusWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SeatService {

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_BOOKED = "BOOKED";
    private static final String STATUS_FUTURE_BOOKED = "FUTURE_BOOKED";
    private static final String STATUS_USED = "USED";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);
    private static final int BOOKING_INTERVAL_MINUTES = 30;
    private static final String[] BUILDINGS = {"B1", "B2", "B3"};
    private static final int[] FLOORS = {1, 2, 3};
    private static final String[] ROWS = {"A", "B", "C", "D", "E"};
    private static final int MAX_COL = 5;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatBookingRepository seatBookingRepository;

    @Autowired
    private SeatViewCacheService seatViewCacheService;

    @Autowired
    private com.example.repository.UserRepository userRepository;

    @Autowired
    private com.example.repository.SystemConfigRepository systemConfigRepository;

    public List<Seat> getAllSeats() {
        return getAllSeats(null, null);
    }

    public List<Seat> getAllSeats(String buildingCode, Integer floorNo) {
        List<Seat> seats = seatRepository.findAll();
        if (seats.isEmpty()) {
            initializeSeats();
            seats = seatRepository.findAll();
        }

        if (!isBlank(buildingCode) && floorNo != null) {
            String normalizedBuildingCode = normalizeBuildingCode(buildingCode);
            Integer normalizedFloorNo = normalizeFloorNo(floorNo);
            return seatRepository.findByBuildingCodeAndFloorNoOrderByRowAscColAsc(
                    normalizedBuildingCode,
                    normalizedFloorNo
            );
        }
        return seats;
    }

    /**
     * 兼容你当前 Controller 里的旧方法签名。
     * timeSlotId / startTime 参数不再参与业务判断，只保留方法名兼容。
     */
    public List<Seat> getSeatsByDateAndTimeSlot(
            LocalDate date,
            String timeSlotId,
            String currentUserId,
            String startTime,
            String buildingCode,
            Integer floorNo
    ) {
        return getSeatsByDate(date, currentUserId, buildingCode, floorNo);
    }

    /**
     * 推荐新用法：按日期获取座位概览状态
     */
    public List<Seat> getSeatsByDate(LocalDate date, String currentUserId, String buildingCode, Integer floorNo) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String normalizedBuildingCode = normalizeBuildingCode(buildingCode);
        Integer normalizedFloorNo = normalizeFloorNo(floorNo);

        if (date.equals(today)) {
            boolean changed = resetExpiredBookings(today, nowTime);
            if (changed) {
                seatViewCacheService.invalidateDate(today);
            }
        }

        List<Seat> cachedSeatViews = seatViewCacheService.get(
                date,
                currentUserId,
                normalizedBuildingCode,
                normalizedFloorNo
        );
        if (cachedSeatViews != null) {
            return cachedSeatViews;
        }

        List<Seat> seats = getAllSeats(normalizedBuildingCode, normalizedFloorNo);
        Set<String> seatIdSet = new HashSet<>();
        for (Seat seat : seats) {
            seatIdSet.add(seat.getSeatId());
        }
        List<SeatBooking> activeBookings = seatBookingRepository.findByStatusOrderByCreatedAtDesc(STATUS_BOOKED);

        Set<String> bookedSeatIds = new HashSet<>();
        Set<String> futureBookedSeatIds = new HashSet<>();
        Map<String, SeatBooking> seatDisplayBookingMap = new HashMap<>();

        for (SeatBooking booking : activeBookings) {
            if (booking.getBookingDate() == null) {
                continue;
            }
            if (!seatIdSet.contains(booking.getSeatId())) {
                continue;
            }

            if (booking.getBookingDate().isEqual(date)) {
                bookedSeatIds.add(booking.getSeatId());

                SeatBooking existing = seatDisplayBookingMap.get(booking.getSeatId());
                if (existing == null) {
                    seatDisplayBookingMap.put(booking.getSeatId(), booking);
                } else {
                    boolean preferCurrentUser = currentUserId != null
                            && !currentUserId.isBlank()
                            && currentUserId.equals(booking.getUserId())
                            && !currentUserId.equals(existing.getUserId());

                    boolean earlierCreated = booking.getCreatedAt() != null
                            && existing.getCreatedAt() != null
                            && booking.getCreatedAt().isBefore(existing.getCreatedAt());

                    if (preferCurrentUser || earlierCreated) {
                        seatDisplayBookingMap.put(booking.getSeatId(), booking);
                    }
                }
            } else if (booking.getBookingDate().isAfter(date)) {
                futureBookedSeatIds.add(booking.getSeatId());
            }
        }

        for (Seat seat : seats) {
            if (bookedSeatIds.contains(seat.getSeatId())) {
                seat.setStatus(STATUS_BOOKED);
                SeatBooking displayBooking = seatDisplayBookingMap.get(seat.getSeatId());
                if (displayBooking != null) {
                    seat.setLastBookedBy(displayBooking.getUserId());
                    seat.setLastBookedAt(displayBooking.getCreatedAt());
                } else {
                    seat.setLastBookedBy(null);
                    seat.setLastBookedAt(null);
                }
            } else if (futureBookedSeatIds.contains(seat.getSeatId())) {
                seat.setStatus(STATUS_FUTURE_BOOKED);
                seat.setLastBookedBy(null);
                seat.setLastBookedAt(null);
            } else {
                seat.setStatus(STATUS_AVAILABLE);
                seat.setLastBookedBy(null);
                seat.setLastBookedAt(null);
            }
        }

        seatViewCacheService.put(date, currentUserId, normalizedBuildingCode, normalizedFloorNo, seats);
        return seats;
    }

    private boolean resetExpiredBookings(LocalDate date, LocalTime nowTime) {
        List<SeatBooking> expiredBookings = seatBookingRepository
                .findByBookingDateAndStatusAndEndTimeLessThanEqual(date, STATUS_BOOKED, nowTime);

        if (expiredBookings.isEmpty()) {
            return false;
        }

        Set<String> affectedSeatIds = new HashSet<>();

        for (SeatBooking booking : expiredBookings) {
            booking.setStatus(STATUS_EXPIRED);
            seatBookingRepository.save(booking);
            // 未打卡的过期预约 → 用户 totalBookings+1 (无效预约)
            if (!Boolean.TRUE.equals(booking.getCheckedIn())) {
                String uid = booking.getUserId();
                if (uid != null) {
                    try {
                        com.example.entity.User user = userRepository.findById(Long.parseLong(uid)).orElse(null);
                        if (user != null) {
                            user.setTotalBookings(user.getTotalBookings() + 1);
                            userRepository.save(user);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            affectedSeatIds.add(booking.getSeatId());
        }

        for (String seatId : affectedSeatIds) {
            refreshSeatStatus(seatId, date);
        }
        return true;
    }

    public Seat getSeatById(Long id) {
        return seatRepository.findById(id).orElse(null);
    }

    public Seat getSeatBySeatId(String seatId) {
        return seatRepository.findBySeatId(seatId);
    }

    public List<Seat> getSeatsByStatus(String status) {
        return seatRepository.findByStatus(status);
    }

    public List<Seat> getAvailableSeats() {
        return seatRepository.findByStatusOrderByRowCol(STATUS_AVAILABLE);
    }

    public List<Seat> getSeatsByRowAndCol(String row, Integer col) {
        return seatRepository.findByRowAndCol(row, col);
    }

    public Seat createSeat(Seat seat) {
        seat.setBuildingCode(normalizeBuildingCode(seat.getBuildingCode()));
        seat.setFloorNo(normalizeFloorNo(seat.getFloorNo()));
        if (isBlank(seat.getSeatId()) && seat.getRow() != null && seat.getCol() != null) {
            seat.setSeatId(buildSeatId(seat.getBuildingCode(), seat.getFloorNo(), seat.getRow(), seat.getCol()));
        }
        if (seat.getNearWindow() == null) {
            seat.setNearWindow(Boolean.FALSE);
        }
        if (seat.getHasOutlet() == null) {
            seat.setHasOutlet(Boolean.FALSE);
        }
        if (seat.getQuietZone() == null) {
            seat.setQuietZone(Boolean.FALSE);
        }
        return seatRepository.save(seat);
    }

    public Seat updateSeat(Long id, Seat seat) {
        Seat existingSeat = seatRepository.findById(id).orElse(null);
        if (existingSeat != null) {
            existingSeat.setSeatId(seat.getSeatId());
            existingSeat.setBuildingCode(normalizeBuildingCode(seat.getBuildingCode()));
            existingSeat.setFloorNo(normalizeFloorNo(seat.getFloorNo()));
            existingSeat.setRow(seat.getRow());
            existingSeat.setCol(seat.getCol());
            existingSeat.setStatus(seat.getStatus());
            existingSeat.setNearWindow(Boolean.TRUE.equals(seat.getNearWindow()));
            existingSeat.setHasOutlet(Boolean.TRUE.equals(seat.getHasOutlet()));
            existingSeat.setQuietZone(Boolean.TRUE.equals(seat.getQuietZone()));
            existingSeat.setLastBookedBy(seat.getLastBookedBy());
            existingSeat.setLastBookedAt(seat.getLastBookedAt());
            return seatRepository.save(existingSeat);
        }
        return null;
    }

    public boolean updateSeatStatus(String seatId, String newStatus) {
        Seat seat = seatRepository.findBySeatId(seatId);
        if (seat != null) {
            seat.setStatus(newStatus);
            seatRepository.save(seat);
            return true;
        }
        return false;
    }

    /**
     * 旧接口保留为兼容层，不再推荐使用。
     * 若必须兼容，可在 Controller 中把 timeSlotId 转为固定时间段后调用新方法。
     */
    public boolean bookSeat(String seatId, String userId, LocalDate bookingDate, String timeSlotId) {
        return false;
    }

    public boolean bookSeatWithDuration(
            String seatId,
            String userId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (isBlank(seatId) || isBlank(userId) || bookingDate == null || startTime == null || endTime == null) {
            return false;
        }

        Seat seat = seatRepository.findBySeatId(seatId);
        if (seat == null) {
            return false;
        }

        if (!isValidBookingTimeRange(startTime, endTime)) {
            return false;
        }

        boolean alreadyBooked = seatBookingRepository
                .existsBySeatIdAndBookingDateAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        seatId,
                        bookingDate,
                        STATUS_BOOKED,
                        endTime,
                        startTime
                );

        if (alreadyBooked) {
            return false;
        }

        SeatBooking booking = new SeatBooking();
        booking.setSeatId(seatId);
        booking.setUserId(userId);
        booking.setBookingDate(bookingDate);
        booking.setTimeSlotId(buildTimeSlotId(startTime, endTime));
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setStatus(STATUS_BOOKED);

        if (!booking.isValidTimeRange()) {
            return false;
        }

        seatBookingRepository.save(booking);

        seat.setStatus(STATUS_BOOKED);
        seat.setLastBookedBy(userId);
        seat.setLastBookedAt(LocalDateTime.now());
        seatRepository.save(seat);

        SeatStatusWebSocketHandler.broadcastSeatStatusUpdate(seat);
        invalidateSeatViewsForBookingDate(bookingDate);
        return true;
    }

    /**
     * 推荐新取消方式：按 bookingId 取消
     */
    public boolean cancelBooking(Long bookingId) {
        if (bookingId == null) {
            return false;
        }

        SeatBooking booking = seatBookingRepository
                .findByIdAndStatus(bookingId, STATUS_BOOKED)
                .orElse(null);

        if (booking == null) {
            return false;
        }

        booking.setStatus(STATUS_CANCELLED);
        seatBookingRepository.save(booking);

        refreshSeatStatus(booking.getSeatId(), booking.getBookingDate());
        invalidateSeatViewsForBookingDate(booking.getBookingDate());
        return true;
    }

    /**
     * 兼容当前前端/Controller 的方式：按 seatId + date + start/end 取消
     */
    public boolean cancelBooking(String seatId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        if (isBlank(seatId) || bookingDate == null || startTime == null || endTime == null) {
            return false;
        }

        SeatBooking booking = seatBookingRepository
                .findBySeatIdAndBookingDateAndStartTimeAndEndTimeAndStatus(
                        seatId,
                        bookingDate,
                        startTime,
                        endTime,
                        STATUS_BOOKED
                )
                .orElse(null);

        if (booking == null) {
            return false;
        }

        booking.setStatus(STATUS_CANCELLED);
        seatBookingRepository.save(booking);

        refreshSeatStatus(seatId, bookingDate);
        invalidateSeatViewsForBookingDate(bookingDate);
        return true;
    }

    public boolean resetSeat(String seatId) {
        Seat seat = seatRepository.findBySeatId(seatId);
        if (seat != null) {
            seat.setStatus(STATUS_AVAILABLE);
            seat.setLastBookedBy(null);
            seat.setLastBookedAt(null);
            seatRepository.save(seat);

            SeatStatusWebSocketHandler.broadcastSeatStatusUpdate(seat);
            seatViewCacheService.invalidateDate(LocalDate.now());
            return true;
        }
        return false;
    }

    public boolean deleteSeat(Long id) {
        if (seatRepository.existsById(id)) {
            seatRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<SeatBooking> getBookingsByUserId(String userId) {
        return seatBookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, STATUS_BOOKED);
    }

    public List<SeatBooking> getBookingsBySeatAndDate(String seatId, LocalDate bookingDate) {
        return seatBookingRepository.findBySeatIdAndBookingDateAndStatusOrderByStartTimeAsc(
                seatId,
                bookingDate,
                STATUS_BOOKED
        );
    }

    public List<SeatBooking> getAllActiveBookings() {
        return seatBookingRepository.findByStatusOrderByCreatedAtDesc(STATUS_BOOKED);
    }

    public List<SeatBooking> getAllBookings() {
        return seatBookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Long getAvailableSeatCount() {
        return seatRepository.countByStatus(STATUS_AVAILABLE);
    }

    public Long getBookedSeatCount() {
        Long count = seatBookingRepository.countByStatus(STATUS_BOOKED);
        return count == null ? 0L : count;
    }

    public Long getUsedSeatCount() {
        return seatRepository.countByStatus(STATUS_USED);
    }

    public Map<String, Object> checkin(Long bookingId, String userId) {
        Map<String, Object> response = new HashMap<>();

        SeatBooking booking = seatBookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            response.put("success", false);
            response.put("message", "预约记录不存在");
            return response;
        }

        if (!booking.getUserId().equals(userId)) {
            response.put("success", false);
            response.put("message", "该预约不属于当前用户");
            return response;
        }

        if (Boolean.TRUE.equals(booking.getCheckedIn())) {
            response.put("success", false);
            response.put("message", "该预约已打卡，无需重复打卡");
            return response;
        }

        if (!"BOOKED".equals(booking.getStatus())) {
            response.put("success", false);
            response.put("message", "该预约状态不允许打卡");
            return response;
        }

        LocalTime now = LocalTime.now();
        if (now.isBefore(booking.getStartTime()) || now.isAfter(booking.getEndTime())) {
            response.put("success", false);
            response.put("message", "不在打卡时间范围内（" + booking.getStartTime() + " - " + booking.getEndTime() + "）");
            return response;
        }

        booking.setCheckedIn(true);
        seatBookingRepository.save(booking);

        try {
            com.example.entity.User user = userRepository.findById(Long.parseLong(userId)).orElse(null);
            if (user != null) {
                user.setTotalBookings(user.getTotalBookings() + 1);
                user.setValidBookings(user.getValidBookings() + 1);
                userRepository.save(user);
            }
        } catch (NumberFormatException ignored) {}

        response.put("success", true);
        response.put("message", "打卡成功");
        return response;
    }

    public Map<String, Object> getUserIntegrity(Long userId) {
        Map<String, Object> result = new HashMap<>();

        com.example.entity.User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            result.put("totalBookings", 0);
            result.put("validBookings", 0);
            result.put("invalidBookings", 0);
            result.put("integrityScore", 1.0);
            return result;
        }

        int total = user.getTotalBookings();
        int valid = user.getValidBookings();
        int invalid = total - valid;
        double score = total > 0 ? (double) valid / total : 1.0;

        result.put("totalBookings", total);
        result.put("validBookings", valid);
        result.put("invalidBookings", invalid);
        result.put("integrityScore", Math.round(score * 10000.0) / 10000.0);
        return result;
    }

    public void initializeSeats() {
        for (String buildingCode : BUILDINGS) {
            for (int floorNo : FLOORS) {
                for (String row : ROWS) {
                    for (int col = 1; col <= MAX_COL; col++) {
                        String seatId = buildSeatId(buildingCode, floorNo, row, col);
                        if (!seatRepository.existsBySeatId(seatId)) {
                            Seat seat = new Seat();
                            seat.setSeatId(seatId);
                            seat.setBuildingCode(buildingCode);
                            seat.setFloorNo(floorNo);
                            seat.setRow(row);
                            seat.setCol(col);
                            seat.setStatus(STATUS_AVAILABLE);
                            seat.setNearWindow(col == MAX_COL);
                            seat.setHasOutlet(col % 2 == 1);
                            seat.setQuietZone("A".equals(row) || "B".equals(row));
                            seatRepository.save(seat);
                        }
                    }
                }
            }
        }
        seatViewCacheService.invalidateDate(LocalDate.now());
    }

    public int randomizeSeatAttributes() {
        List<Seat> seats = seatRepository.findAll();
        if (seats.isEmpty()) {
            return 0;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Seat seat : seats) {
            seat.setNearWindow(random.nextBoolean());
            seat.setHasOutlet(random.nextBoolean());
            seat.setQuietZone(random.nextBoolean());
        }

        seatRepository.saveAll(seats);
        LocalDate today = LocalDate.now();
        seatViewCacheService.invalidateDateRange(today, today.plusDays(30));
        return seats.size();
    }

    private boolean isValidBookingTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            return false;
        }

        if (startTime.isBefore(OPEN_TIME) || endTime.isAfter(CLOSE_TIME)) {
            return false;
        }

        if (startTime.getMinute() % BOOKING_INTERVAL_MINUTES != 0
                || endTime.getMinute() % BOOKING_INTERVAL_MINUTES != 0) {
            return false;
        }

        return !startTime.plusMinutes(BOOKING_INTERVAL_MINUTES).isAfter(endTime);
    }

    private void refreshSeatStatus(String seatId, LocalDate date) {
        Seat seat = seatRepository.findBySeatId(seatId);
        if (seat == null) {
            return;
        }

        boolean hasCurrentDayBooking = !seatBookingRepository
                .findBySeatIdAndBookingDateAndStatusOrderByStartTimeAsc(seatId, date, STATUS_BOOKED)
                .isEmpty();

        boolean hasFutureBooking = seatBookingRepository
                .findByStatusOrderByCreatedAtDesc(STATUS_BOOKED)
                .stream()
                .anyMatch(booking ->
                        seatId.equals(booking.getSeatId())
                                && booking.getBookingDate() != null
                                && booking.getBookingDate().isAfter(date)
                );

        if (hasCurrentDayBooking) {
            List<SeatBooking> currentBookings = seatBookingRepository
                    .findBySeatIdAndBookingDateAndStatusOrderByStartTimeAsc(seatId, date, STATUS_BOOKED);

            SeatBooking latestBooking = currentBookings.get(currentBookings.size() - 1);
            seat.setStatus(STATUS_BOOKED);
            seat.setLastBookedBy(latestBooking.getUserId());
            seat.setLastBookedAt(latestBooking.getCreatedAt());
        } else if (hasFutureBooking) {
            seat.setStatus(STATUS_FUTURE_BOOKED);
            seat.setLastBookedBy(null);
            seat.setLastBookedAt(null);
        } else {
            seat.setStatus(STATUS_AVAILABLE);
            seat.setLastBookedBy(null);
            seat.setLastBookedAt(null);
        }

        seatRepository.save(seat);
        SeatStatusWebSocketHandler.broadcastSeatStatusUpdate(seat);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String buildTimeSlotId(LocalTime startTime, LocalTime endTime) {
        return "custom-" + startTime.toString().replace(":", "") + "-" + endTime.toString().replace(":", "");
    }

    private void invalidateSeatViewsForBookingDate(LocalDate bookingDate) {
        if (bookingDate == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        if (bookingDate.isAfter(today)) {
            seatViewCacheService.invalidateDateRange(today, bookingDate);
        } else {
            seatViewCacheService.invalidateDate(bookingDate);
        }
    }

    private String buildSeatId(String buildingCode, Integer floorNo, String row, Integer col) {
        return buildingCode + "-F" + floorNo + "-" + row + "-" + col;
    }

    private String normalizeBuildingCode(String buildingCode) {
        if (buildingCode == null || buildingCode.isBlank()) {
            return "B1";
        }
        String normalized = buildingCode.trim().toUpperCase();
        for (String candidate : BUILDINGS) {
            if (candidate.equals(normalized)) {
                return normalized;
            }
        }
        return "B1";
    }

    private Integer normalizeFloorNo(Integer floorNo) {
        if (floorNo == null) {
            return 1;
        }
        for (int candidate : FLOORS) {
            if (candidate == floorNo) {
                return floorNo;
            }
        }
        return 1;
    }
}
