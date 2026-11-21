package com.example.spucs;

public class Account {
    private String accountId;
    private String email;
    // Plain text is acceptable only because this is fixed demo data.
    private String password;

    public Account(String accountId, String email, String password) {
        this.accountId = accountId;
        this.email = email;
        this.password = password;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getEmail() {
        return email;
    }
}
