package com.example.spucs;

import java.util.ArrayList;

public class Parent extends User {
    private String pin;
    // True while the parent has locked the child's phone (Lock in the class diagram).
    public boolean Lock;
    private Account account;
    private ArrayList<Child> children = new ArrayList<>();

    public Parent(int userID, String name, String pin, Account account) {
        super(userID, name);
        this.pin = pin;
        this.account = account;
    }

    public Child addChild(String name, int age) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter the child's name.");
        }
        if (age < 3 || age > 17) {
            throw new IllegalArgumentException("Age must be a number from 3 to 17.");
        }
        Child child = new Child(children.size() + 1, name.trim(), age, this);
        children.add(child);
        return child;
    }

    public boolean verifyPIN(String pin) {
        return this.pin.equals(pin);
    }

    public Account getAccount() {
        return account;
    }

    public ArrayList<Child> getChildren() {
        return children;
    }
}
