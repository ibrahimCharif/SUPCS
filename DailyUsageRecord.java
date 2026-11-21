package com.example.spucs;

import java.time.LocalDate;
import java.util.ArrayList;

public class DailyUsageRecord {
    private LocalDate date;
    private long usedSeconds;
    private Child child;
    private ArrayList<ExtraTimeRequest> requests = new ArrayList<>();

    public DailyUsageRecord(LocalDate date, Child child) {
        this.date = date;
        this.child = child;
    }

    // Adds usage, but never more than the time that is left. Returns the seconds added.
    public long addUsage(long seconds) {
        long added = Math.min(seconds, getRemainingSeconds());
        usedSeconds = usedSeconds + added;
        return added;
    }

    // Derived (/extraMinutes): the minutes of this day's approved requests.
    public int getExtraMinutes() {
        int total = 0;
        for (ExtraTimeRequest request : requests) {
            if (request.getStatus().equals(ExtraTimeRequest.APPROVED)) {
                total = total + request.getGrantedMinutes();
            }
        }
        return total;
    }

    // Derived (/remainingSeconds): (daily limit + extra minutes) * 60 - used seconds, never below 0.
    public long getRemainingSeconds() {
        long allowedSeconds = (child.getDailyLimitMin() + getExtraMinutes()) * 60L;
        return Math.max(0, allowedSeconds - usedSeconds);
    }

    public void addRequest(ExtraTimeRequest request) {
        requests.add(request);
    }

    public LocalDate getDate() {
        return date;
    }

    public long getUsedSeconds() {
        return usedSeconds;
    }
}
