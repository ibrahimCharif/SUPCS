package com.example.spucs;

import java.time.LocalDate;
import java.util.ArrayList;

public class Child extends User {
    private int age;
    // 0 means the parent has not set a limit yet, so there is no normal-use allowance.
    private int dailyLimitMin;
    private boolean hasConsent;
    private Parent parent;
    private FaceTemplate faceTemplate;
    private ArrayList<DailyUsageRecord> dailyRecords = new ArrayList<>();

    public Child(int userID, String name, int age, Parent parent) {
        super(userID, name);
        this.age = age;
        this.parent = parent;
    }

    // Receives the limit the parent chose, stores it and returns it.
    public int getDailyLimit(int minutes) {
        if (minutes < 1 || minutes > 1440) {
            throw new IllegalArgumentException("The daily limit must be from 1 to 1440 minutes.");
        }
        dailyLimitMin = minutes;
        return dailyLimitMin;
    }

    // Receives the parent's consent answer for face enrollment, stores it and returns it.
    public boolean getConsent(boolean value) {
        hasConsent = value;
        return hasConsent;
    }

    // One record per date: returns the record for this date and creates it the first time.
    public DailyUsageRecord getUsageRecord(LocalDate date) {
        for (DailyUsageRecord record : dailyRecords) {
            if (record.getDate().equals(date)) {
                return record;
            }
        }
        DailyUsageRecord record = new DailyUsageRecord(date, this);
        dailyRecords.add(record);
        return record;
    }

    public int getAge() {
        return age;
    }

    public int getDailyLimitMin() {
        return dailyLimitMin;
    }

    public boolean hasConsent() {
        return hasConsent;
    }

    public Parent getParent() {
        return parent;
    }

    public FaceTemplate getFaceTemplate() {
        return faceTemplate;
    }

    public void setFaceTemplate(FaceTemplate faceTemplate) {
        this.faceTemplate = faceTemplate;
    }

    public ArrayList<DailyUsageRecord> getDailyRecords() {
        return dailyRecords;
    }
}
