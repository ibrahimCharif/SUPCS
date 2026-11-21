package com.example.spucs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// The first screen: Log In or Create Account.
public class LoginActivity extends Activity {
    // Shared with MainActivity. Static, so it stays while the app is open.
    static AccountManager accountManager = new AccountManager();

    private View loginSection;
    private View registerSection;
    private TextView messageText;
    private EditText loginEmailInput;
    private EditText loginPasswordInput;
    private EditText nameInput;
    private EditText registerEmailInput;
    private EditText registerPasswordInput;
    private EditText registerPinInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (accountManager.getLoggedInParent() != null) {
            openMainScreen();
            return;
        }
        setContentView(R.layout.activity_login);
        loginSection = findViewById(R.id.loginSection);
        registerSection = findViewById(R.id.registerSection);
        messageText = findViewById(R.id.loginMessageText);
        loginEmailInput = findViewById(R.id.loginEmailInput);
        loginPasswordInput = findViewById(R.id.loginPasswordInput);
        nameInput = findViewById(R.id.nameInput);
        registerEmailInput = findViewById(R.id.registerEmailInput);
        registerPasswordInput = findViewById(R.id.registerPasswordInput);
        registerPinInput = findViewById(R.id.registerPinInput);

        findViewById(R.id.showLoginButton).setOnClickListener(v -> showLogin(true));
        findViewById(R.id.showRegisterButton).setOnClickListener(v -> showLogin(false));
        findViewById(R.id.loginButton).setOnClickListener(v -> onLogIn());
        findViewById(R.id.registerButton).setOnClickListener(v -> onCreateAccount());
        showLogin(true);
    }

    private void showLogin(boolean login) {
        loginSection.setVisibility(login ? View.VISIBLE : View.GONE);
        registerSection.setVisibility(login ? View.GONE : View.VISIBLE);
    }

    private void onLogIn() {
        try {
            accountManager.login(text(loginEmailInput), text(loginPasswordInput));
            openMainScreen();
        } catch (IllegalArgumentException e) {
            show(e.getMessage());
        }
    }

    private void onCreateAccount() {
        try {
            accountManager.register(text(nameInput), text(registerEmailInput), text(registerPasswordInput),
                    text(registerPinInput));
            openMainScreen();
        } catch (IllegalArgumentException e) {
            show(e.getMessage());
        }
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String text(EditText input) {
        return input.getText().toString();
    }

    private void show(String message) {
        messageText.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
