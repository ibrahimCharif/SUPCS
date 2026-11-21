package com.example.spucs;

import android.app.Activity;
import android.content.Intent;

public class ChildDevice {
    private int deviceID;
    private Parent pairedParent;
    // The child using the phone now: zero or one.
    private Child activeChild;
    private DeviceLock lock;

    public ChildDevice(int deviceID) {
        this.deviceID = deviceID;
        this.lock = new DeviceLock("Child Phone #" + deviceID);
    }

    public boolean pair(Parent parent) {
        if (parent == null) {
            return false;
        }
        pairedParent = parent;
        return true;
    }

    // Opens the camera screen. The face picture comes back to the screen's onActivityResult.
    public void captureFace(Activity screen, int requestCode) {
        screen.startActivityForResult(new Intent(screen, FaceCaptureActivity.class), requestCode);
    }

    // The device passes the request to the UsageController.
    public ExtraTimeRequest requestMoreTime(UsageController controller, int minutes) {
        return controller.requestExtraTime(this, minutes);
    }

    public int getDeviceID() {
        return deviceID;
    }

    public Parent getPairedParent() {
        return pairedParent;
    }

    public Account getPairedAccount() {
        return pairedParent.getAccount();
    }

    public Child getActiveChild() {
        return activeChild;
    }

    public void setActiveChild(Child child) {
        activeChild = child;
    }

    public DeviceLock getLock() {
        return lock;
    }
}
