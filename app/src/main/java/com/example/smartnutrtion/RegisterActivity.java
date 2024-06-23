package com.example.smartnutrtion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private TextView loginRedirectTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        emailEditText = findViewById(R.id.email_register);
        passwordEditText = findViewById(R.id.password_register);
        confirmPasswordEditText = findViewById(R.id.confirm_password_register);
        registerButton = findViewById(R.id.register_button);
        loginRedirectTextView = findViewById(R.id.login_redirect);

        registerButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (email.isEmpty()) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.setError("Please confirm your password");
                confirmPasswordEditText.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Passwords do not match");
                confirmPasswordEditText.requestFocus();
                return;
            }

            registerUser(email, password);
        });

        loginRedirectTextView.setOnClickListener(view -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    public void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            createUserProfile(uid, email);
                        }
                        Toast.makeText(RegisterActivity.this, "Registration successful.",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Login.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void createUserProfile(String uid, String email) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("email", email);
        userProfile.put("name", "");
        userProfile.put("age", 0);
        userProfile.put("weight", 0);
        userProfile.put("height", 0);
        userProfile.put("goal", "");

        databaseReference.child(uid).setValue(userProfile).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this, "Failed to create user profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
