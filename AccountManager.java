package com.example.spucs;

import java.util.ArrayList;

// Creates parent accounts and logs parents in and out.
// Like all Version 2 data, the accounts are kept in memory until the app is closed.
public class AccountManager {
    private ArrayList<Parent> parents = new ArrayList<>();
    private Parent loggedInParent;

    public AccountManager() {
        // A ready-made account for quick demos.
        register("Demo Parent", "parent@spucs.demo", "demo1234", "1234");
        logout();
    }

    // Create Account: checks the fields, saves the new parent and logs them in.
    public Parent register(String name, String email, String password, String pin) {
        name = name.trim();
        email = email.trim().toLowerCase();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Enter your name.");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("The password needs at least 6 characters.");
        }
        if (!pin.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("The parent PIN must be 4 digits.");
        }
        if (findParent(email) != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        int number = parents.size() + 1;
        Account account = new Account("ACC-" + number, email, password);
        Parent parent = new Parent(number, name, pin, account);
        parents.add(parent);
        loggedInParent = parent;
        return parent;
    }

    // Log In: the email must belong to an account and the password must match.
    public Parent login(String email, String password) {
        Parent parent = findParent(email.trim().toLowerCase());
        if (parent == null || !parent.getAccount().authenticate(password)) {
            throw new IllegalArgumentException("Wrong email or password.");
        }
        loggedInParent = parent;
        return parent;
    }

    public void logout() {
        loggedInParent = null;
    }

    public Parent getLoggedInParent() {
        return loggedInParent;
    }

    private Parent findParent(String email) {
        for (Parent parent : parents) {
            if (parent.getAccount().getEmail().equals(email)) {
                return parent;
            }
        }
        return null;
    }
}
