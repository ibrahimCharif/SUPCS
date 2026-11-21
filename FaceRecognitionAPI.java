package com.example.spucs;

import android.graphics.Bitmap;

import java.util.ArrayList;

// In Version 2 an "image" is a face picture (Bitmap) from the camera.
public interface FaceRecognitionAPI {
    // Saves the face template and returns its reference.
    String enroll(Bitmap face);

    // Returns the reference (out of these references) that matches the face, or null.
    String match(Bitmap face, ArrayList<String> references);

    boolean checkLiveness(Bitmap face);

    void deleteEnrollment(String reference);
}
