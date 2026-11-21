package com.example.spucs;

import java.time.LocalDate;
import java.util.ArrayList;

public class UsageController {
    // The demo date. "Next Day - Demo" moves it forward, so one date is used everywhere.
    private LocalDate today = LocalDate.now();
    private ArrayList<ExtraTimeRequest> requests = new ArrayList<>();
    private ArrayList<String> notifications = new ArrayList<>();
    private int nextRequestNumber = 1;

    public void configureDailyLimit(Parent parent, Child child, int mins) {
        if (child.getParent() != parent) {
            throw new IllegalArgumentException("Choose one of this parent's children.");
        }
        child.getDailyLimit(mins);
        notifications.add("Daily limit for " + child.getName() + " set to " + mins + " minutes.");
    }

    // Parent.Lock: true locks the child's phone, false removes the parent lock.
    public void lockDevice(Parent parent, boolean lock) {
        parent.Lock = lock;
        if (lock) {
            notifications.add("You locked the child's phone.");
        } else {
            notifications.add("You unlocked the child's phone.");
        }
    }

    // UC-10 Lock Phone. Returns the seconds that were recorded.
    public long recordUsage(ChildDevice device, long seconds) {
        Child child = device.getActiveChild();
        if (child == null) {
            throw new IllegalStateException("Identify a child first.");
        }
        evaluateLock(device);
        if (device.getLock().isLocked()) {
            throw new IllegalStateException("The phone is restricted, so no usage is recorded.");
        }
        long added = getTodayRecord(child).addUsage(seconds);
        evaluateLock(device);
        return added;
    }

    // UC-13 Request More Time.
    public ExtraTimeRequest requestExtraTime(ChildDevice device, int minutes) {
        Child child = device.getActiveChild();
        if (child == null) {
            throw new IllegalStateException("Identify a child first.");
        }
        if (minutes < 1 || minutes > 240) {
            throw new IllegalArgumentException("Ask for 1 to 240 minutes.");
        }
        DailyUsageRecord record = getTodayRecord(child);
        ExtraTimeRequest request = new ExtraTimeRequest("R" + nextRequestNumber, minutes, child, record, device);
        nextRequestNumber++;
        record.addRequest(request);
        requests.add(request);
        notifyParent(request);
        return request;
    }

    // decision true approves with mins extra minutes; false denies.
    public void reviewRequest(Parent parent, ExtraTimeRequest request, boolean decision, int mins) {
        if (decision) {
            request.approve(mins);
            notifications.add("Approved " + mins + " extra minutes for " + request.getChild().getName() + ".");
        } else {
            request.deny();
            notifications.add("Denied request " + request.getRequestId() + ".");
        }
        request.setReviewedBy(parent);
        // The minutes belong to the request's own child and day, even if another child is active now.
        evaluateLock(request.getDevice());
    }

    // Applies or releases the restrictions for the active child and sets the 15 and 5 minute warnings.
    public void evaluateLock(ChildDevice device) {
        DeviceLock lock = device.getLock();
        if (device.getPairedParent().Lock) {
            lock.enforce(DeviceLock.PARENT_LOCK);
        } else {
            lock.release(DeviceLock.PARENT_LOCK);
        }
        Child child = device.getActiveChild();
        if (child == null) {
            lock.release(DeviceLock.DAILY_LIMIT);
            lock.showWarning(0);
            return;
        }
        long remaining = getTodayRecord(child).getRemainingSeconds();
        if (remaining == 0) {
            lock.enforce(DeviceLock.DAILY_LIMIT);
            lock.showWarning(0);
        } else {
            lock.release(DeviceLock.DAILY_LIMIT);
            if (remaining <= 5 * 60) {
                lock.showWarning(5);
            } else if (remaining <= 15 * 60) {
                lock.showWarning(15);
            } else {
                lock.showWarning(0);
            }
        }
    }

    // The parent types the PIN on the child's phone to remove the parent lock.
    public boolean unlockWithParentPIN(ChildDevice device, String pin) {
        Parent parent = device.getPairedParent();
        if (!parent.verifyPIN(pin)) {
            return false;
        }
        parent.Lock = false;
        evaluateLock(device);
        return true;
    }

    public void notifyParent(ExtraTimeRequest request) {
        notifications.add(request.getChild().getName() + " asks for " + request.getRequestedMinutes()
                + " more minutes (" + request.getRequestId() + ").");
    }

    // Daily reset for the demo: new records start from zero and old pending requests expire.
    public void nextDay(ChildDevice device) {
        today = today.plusDays(1);
        for (ExtraTimeRequest request : requests) {
            request.expire();
        }
        notifications.add("New day: " + today + ". Usage starts from zero.");
        evaluateLock(device);
    }

    public DailyUsageRecord getTodayRecord(Child child) {
        return child.getUsageRecord(today);
    }

    public LocalDate getToday() {
        return today;
    }

    public ArrayList<ExtraTimeRequest> getRequests() {
        return requests;
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }
}
