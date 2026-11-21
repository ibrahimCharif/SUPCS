package com.example.spucs;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class FaceRecognitionService {
    // Version 2 uses the AI (OnDeviceFaceRecognitionAPI).
    private FaceRecognitionAPI api;

    public FaceRecognitionService(FaceRecognitionAPI api) {
        this.api = api;
    }

    public FaceTemplate enrollFace(Parent parent, Child child, Bitmap image) {
        if (child.getParent() != parent) {
            throw new IllegalArgumentException("This child does not belong to this parent.");
        }
        if (!child.hasConsent()) {
            throw new IllegalStateException("Enrollment failed: consent was withheld for " + child.getName()
                    + ". The profile is still saved.");
        }
        if (!api.checkLiveness(image)) {
            throw new IllegalStateException("Enrollment failed: the picture is too dark or too bright. "
                    + "Try again in better light. The profile is still saved.");
        }
        // Enrolling again replaces the old face.
        deleteFaceData(parent, child);
        FaceTemplate template = new FaceTemplate(api.enroll(image));
        child.setFaceTemplate(template);
        return template;
    }

    // Returns the recognised child, or null. A failed attempt never leaves the previous child active.
    public Child identifyChild(ChildDevice device, Bitmap image) {
        device.setActiveChild(null);
        if (!api.checkLiveness(image)) {
            return null;
        }
        // Only the faces of this parent's children are compared.
        ArrayList<String> references = new ArrayList<>();
        for (Child child : device.getPairedParent().getChildren()) {
            if (child.getFaceTemplate() != null) {
                references.add(child.getFaceTemplate().getReference());
            }
        }
        String reference = api.match(image, references);
        for (Child child : device.getPairedParent().getChildren()) {
            FaceTemplate template = child.getFaceTemplate();
            if (template != null && template.getReference().equals(reference)) {
                device.setActiveChild(child);
                return child;
            }
        }
        return null;
    }

    // Fallback when recognition does not work. A correct PIN only selects the child; it adds no time.
    public boolean selectChildWithPIN(ChildDevice device, Child child, String pin) {
        device.setActiveChild(null);
        Parent parent = device.getPairedParent();
        if (child == null || child.getParent() != parent || !parent.verifyPIN(pin)) {
            return false;
        }
        device.setActiveChild(child);
        return true;
    }

    public void deleteFaceData(Parent parent, Child child) {
        if (child.getParent() == parent && child.getFaceTemplate() != null) {
            api.deleteEnrollment(child.getFaceTemplate().getReference());
            child.setFaceTemplate(null);
        }
    }
}
