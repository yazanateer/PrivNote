package com.example.privnote;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextPin;
    private MaterialButton buttonLogin;

    private static final String CORRECT_PIN = "1234";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViews();
        setupListeners();
    }

    private void findViews() {
        editTextPin = findViewById(R.id.editTextPin);
        buttonLogin = findViewById(R.id.buttonLogin);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(view -> {
            String enteredPin = editTextPin.getText() != null ? editTextPin.getText().toString().trim() : "";

            if (TextUtils.isEmpty(enteredPin)) {
                Toast.makeText(this, "Please enter your PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredPin.equals(CORRECT_PIN)) {
                // Go to the next activity (Notes)
                Intent intent = new Intent(LoginActivity.this, NotesActivity.class);
                startActivity(intent);
                finish(); // close login screen
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }
}