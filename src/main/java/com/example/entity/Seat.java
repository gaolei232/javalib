package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_id", unique = true, nullable = false, length = 20)
    private String seatId;

    @Column(name = "building_code", nullable = false, length = 20)
    private String buildingCode;

    @Column(name = "floor_no", nullable = false)
    private Integer floorNo;

    @Column(name = "seat_row", nullable = false, length = 10)
    private String row;

    @Column(name = "col", nullable = false)
    private Integer col;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "near_window", nullable = false)
    private Boolean nearWindow = false;

    @Column(name = "has_outlet", nullable = false)
    private Boolean hasOutlet = false;

    @Column(name = "quiet_zone", nullable = false)
    private Boolean quietZone = false;

    @Column(name = "last_booked_by", length = 255)
    private String lastBookedBy;

    @Column(name = "last_booked_at")
    private java.time.LocalDateTime lastBookedAt;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public Integer getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(Integer floorNo) {
        this.floorNo = floorNo;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public Integer getCol() {
        return col;
    }

    public void setCol(Integer col) {
        this.col = col;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getNearWindow() {
        return nearWindow;
    }

    public void setNearWindow(Boolean nearWindow) {
        this.nearWindow = nearWindow;
    }

    public Boolean getHasOutlet() {
        return hasOutlet;
    }

    public void setHasOutlet(Boolean hasOutlet) {
        this.hasOutlet = hasOutlet;
    }

    public Boolean getQuietZone() {
        return quietZone;
    }

    public void setQuietZone(Boolean quietZone) {
        this.quietZone = quietZone;
    }

    public String getLastBookedBy() {
        return lastBookedBy;
    }

    public void setLastBookedBy(String lastBookedBy) {
        this.lastBookedBy = lastBookedBy;
    }

    public java.time.LocalDateTime getLastBookedAt() {
        return lastBookedAt;
    }

    public void setLastBookedAt(java.time.LocalDateTime lastBookedAt) {
        this.lastBookedAt = lastBookedAt;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
