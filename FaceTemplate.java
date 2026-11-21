package com.example.spucs;

import java.time.LocalDateTime;

public class FaceTemplate {
    // The face numbers themselves are kept by the face recognition API. No photo is kept.
    private String enrollmentRef;
    private LocalDateTime createdAt;

    public FaceTemplate(String enrollmentRef) {
        this.enrollmentRef = enrollmentRef;
        this.createdAt = LocalDateTime.now();
    }

    public String getReference() {
        return enrollmentRef;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
