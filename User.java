package com.example.spucs;

// Common identity of Parent and Child.
public abstract class User {
    protected int userID;
    protected String name;

    public User(int userID, String name) {
        this.userID = userID;
        this.name = name;
    }

    public int getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }
}
