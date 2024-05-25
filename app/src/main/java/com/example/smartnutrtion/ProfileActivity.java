package com.example.smartnutrtion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText editUserName;
    private EditText editUserAge;
    private EditText editUserWeight;
    private EditText editUserHeight;
    private Spinner editUserGoal;
    private Button saveProfileButton;
    private Button backToHomeButton;
    private Button logoutButton;
    private ImageView profileImage;
    private TextView userReportMessage;
    private TextView userBMI;
    private TextView userCalories;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editUserName = findViewById(R.id.editUserName);
        editUserAge = findViewById(R.id.editUserAge);
        editUserWeight = findViewById(R.id.editUserWeight);
        editUserHeight = findViewById(R.id.editUserHeight);
        editUserGoal = findViewById(R.id.editUserGoal);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        backToHomeButton = findViewById(R.id.backToHomeButton);
        logoutButton = findViewById(R.id.logoutButton);
        profileImage = findViewById(R.id.profileImage);
        userReportMessage = findViewById(R.id.userReportMessage);
        userBMI = findViewById(R.id.userBMI);
        userCalories = findViewById(R.id.userCalories);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_goals, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editUserGoal.setAdapter(adapter);

        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);
            loadUserProfile();
        }

        saveProfileButton.setOnClickListener(v -> saveUserProfile());
        backToHomeButton.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, HomePageActivity.class)));
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String name = dataSnapshot.child("name").getValue(String.class);
                    Long age = dataSnapshot.child("age").getValue(Long.class);
                    Long weight = dataSnapshot.child("weight").getValue(Long.class);
                    Long height = dataSnapshot.child("height").getValue(Long.class);
                    String goal = dataSnapshot.child("goal").getValue(String.class);

                    if (name != null) editUserName.setText(name);
                    if (age != null) editUserAge.setText(String.valueOf(age));
                    if (weight != null) editUserWeight.setText(String.valueOf(weight));
                    if (height != null) editUserHeight.setText(String.valueOf(height));
                    if (goal != null) {
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) editUserGoal.getAdapter();
                        int position = adapter.getPosition(goal);
                        editUserGoal.setSelection(position);
                    }

                    if (age != null && weight != null && height != null) {
                        userReportMessage.setVisibility(View.GONE);
                        calculateAndDisplayUserReport(age, weight, height);
                    } else {
                        userReportMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    userReportMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        String name = editUserName.getText().toString().trim();
        String ageStr = editUserAge.getText().toString().trim();
        String weightStr = editUserWeight.getText().toString().trim();
        String heightStr = editUserHeight.getText().toString().trim();
        String goal = editUserGoal.getSelectedItem().toString();

        if (name.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Long age = Long.parseLong(ageStr);
        Long weight = Long.parseLong(weightStr);
        Long height = Long.parseLong(heightStr);

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("age", age);
        userProfile.put("weight", weight);
        userProfile.put("height", height);
        userProfile.put("goal", goal);

        databaseReference.setValue(userProfile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                calculateAndDisplayUserReport(age, weight, height);
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndDisplayUserReport(Long age, Long weight, Long height) {
        double bmi = weight / Math.pow(height / 100.0, 2);
        int dailyCalories = calculateDailyCalories(age, weight, height);

        userBMI.setText(String.format("BMI: %.2f", bmi));
        userCalories.setText(String.format("Calorii necesare pe zi: %d", dailyCalories));

        userBMI.setVisibility(View.VISIBLE);
        userCalories.setVisibility(View.VISIBLE);
        userReportMessage.setVisibility(View.GONE);
    }

    private int calculateDailyCalories(Long age, Long weight, Long height) {
        // Basic estimation using Mifflin-St Jeor Equation
        return (int) (10 * weight + 6.25 * height - 5 * age + 5); // for males
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}
