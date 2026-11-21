package com.example.spucs;

import java.util.ArrayList;

public class DeviceLock {
    public static final String DAILY_LIMIT = "Daily limit reached";
    public static final String PARENT_LOCK = "Locked by the parent";

    private String device;
    private ArrayList<String> reasons = new ArrayList<>();
    private String warning = "";

    public DeviceLock(String device) {
        this.device = device;
    }

    // Derived (/isLocked): locked while at least one reason is active.
    public boolean isLocked() {
        return !reasons.isEmpty();
    }

    public void enforce(String reason) {
        if (!reasons.contains(reason)) {
            reasons.add(reason);
        }
    }

    // Removes only this reason; another active reason keeps the phone restricted.
    public void release(String reason) {
        reasons.remove(reason);
    }

    // 0 means no warning.
    public void showWarning(int minutes) {
        if (minutes == 0) {
            warning = "";
        } else {
            warning = "Warning: " + minutes + " minutes or less of screen time left today.";
        }
    }

    // Demonstration only: no call is placed and the restriction stays.
    public String allowEmergencyAccess() {
        return "Emergency access (demo): a real phone would open the emergency dialer here. "
                + "No call was placed and " + device + " is still restricted.";
    }

    public ArrayList<String> getReasons() {
        return reasons;
    }

    public String getWarning() {
        return warning;
    }

    public String getDevice() {
        return device;
    }
}
