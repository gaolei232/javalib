package com.example.websocket;

import com.example.entity.Seat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SeatStatusWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket连接已建立: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        System.out.println("WebSocket连接已关闭: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);
    }

    public static void broadcastSeatStatusUpdate(Seat seat) {
        if (seat == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "seatStatusUpdate");
        payload.put("seat", new SeatMessage(seat));

        broadcastPayload(payload);
    }

    public static void broadcastBookingUpdate(Map<String, Object> bookingPayload) {
        if (bookingPayload == null) {
            return;
        }

        broadcastPayload(bookingPayload);
    }

    private static void broadcastPayload(Map<String, Object> payload) {
        try {
            String message = OBJECT_MAPPER.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(message);

            Iterator<Map.Entry<String, WebSocketSession>> iterator = sessions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, WebSocketSession> entry = iterator.next();
                WebSocketSession session = entry.getValue();

                try {
                    if (session != null && session.isOpen()) {
                        session.sendMessage(textMessage);
                    } else {
                        iterator.remove();
                    }
                } catch (IOException e) {
                    iterator.remove();
                    System.err.println("WebSocket消息发送失败, sessionId=" + entry.getKey());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("WebSocket消息序列化失败");
            e.printStackTrace();
        }
    }

    private static class SeatMessage {
        private final String seatId;
        private final String buildingCode;
        private final Integer floorNo;
        private final String row;
        private final Integer col;
        private final String status;
        private final Boolean nearWindow;
        private final Boolean hasOutlet;
        private final Boolean quietZone;
        private final String lastBookedBy;
        private final String lastBookedAt;

        public SeatMessage(Seat seat) {
            this.seatId = seat.getSeatId();
            this.buildingCode = seat.getBuildingCode();
            this.floorNo = seat.getFloorNo();
            this.row = seat.getRow();
            this.col = seat.getCol();
            this.status = seat.getStatus();
            this.nearWindow = seat.getNearWindow();
            this.hasOutlet = seat.getHasOutlet();
            this.quietZone = seat.getQuietZone();
            this.lastBookedBy = seat.getLastBookedBy();
            this.lastBookedAt = seat.getLastBookedAt() != null ? seat.getLastBookedAt().toString() : null;
        }

        public String getSeatId() {
            return seatId;
        }

        public String getBuildingCode() {
            return buildingCode;
        }

        public Integer getFloorNo() {
            return floorNo;
        }

        public String getRow() {
            return row;
        }

        public Integer getCol() {
            return col;
        }

        public String getStatus() {
            return status;
        }

        public Boolean getNearWindow() {
            return nearWindow;
        }

        public Boolean getHasOutlet() {
            return hasOutlet;
        }

        public Boolean getQuietZone() {
            return quietZone;
        }

        public String getLastBookedBy() {
            return lastBookedBy;
        }

        public String getLastBookedAt() {
            return lastBookedAt;
        }
    }
}
