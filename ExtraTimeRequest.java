package com.example.spucs;

import java.time.LocalDateTime;

public class ExtraTimeRequest {
    public static final String PENDING = "Pending";
    public static final String APPROVED = "Approved";
    public static final String DENIED = "Denied";
    public static final String EXPIRED = "Expired";

    private String requestId;
    private LocalDateTime createdAt;
    private int requestedMinutes;
    private int grantedMinutes;
    private String status = PENDING;
    private Child child;
    private DailyUsageRecord record;
    private ChildDevice device;
    // Stays null while the request is pending.
    private Parent reviewedBy;

    public ExtraTimeRequest(String requestId, int requestedMinutes, Child child, DailyUsageRecord record,
                            ChildDevice device) {
        this.requestId = requestId;
        this.createdAt = LocalDateTime.now();
        this.requestedMinutes = requestedMinutes;
        this.child = child;
        this.record = record;
        this.device = device;
    }

    public void approve(int minutes) {
        checkPending();
        if (minutes < 1 || minutes > 240) {
            throw new IllegalArgumentException("Grant from 1 to 240 minutes.");
        }
        grantedMinutes = minutes;
        status = APPROVED;
    }

    public void deny() {
        checkPending();
        status = DENIED;
    }

    // Only a pending request can expire.
    public void expire() {
        if (status.equals(PENDING)) {
            status = EXPIRED;
        }
    }

    // A request is decided only once, so extra minutes are never added twice.
    private void checkPending() {
        if (!status.equals(PENDING)) {
            throw new IllegalStateException("Request " + requestId + " is already " + status.toLowerCase() + ".");
        }
    }

    public void setReviewedBy(Parent parent) {
        reviewedBy = parent;
    }

    public String getRequestId() {
        return requestId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getRequestedMinutes() {
        return requestedMinutes;
    }

    public int getGrantedMinutes() {
        return grantedMinutes;
    }

    public String getStatus() {
        return status;
    }

    public Child getChild() {
        return child;
    }

    public DailyUsageRecord getRecord() {
        return record;
    }

    public ChildDevice getDevice() {
        return device;
    }

    public Parent getReviewedBy() {
        return reviewedBy;
    }
}
