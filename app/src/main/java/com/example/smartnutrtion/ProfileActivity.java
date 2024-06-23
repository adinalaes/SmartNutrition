package com.example.smartnutrtion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText editUserName;
    private EditText editUserAge;
    private EditText editUserWeight;
    private EditText editUserHeight;
    private Spinner editUserSex;
    private Button saveProfileButton;
    private Button backToHomeButton;
    private Button logoutButton;
    private Button generateMealPlanButton;
    private ImageView profileImage;
    private TextView userReportMessage;
    protected TextView userBMI;
    protected TextView userCalories;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private List<Recipe> recipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editUserName = findViewById(R.id.editUserName);
        editUserAge = findViewById(R.id.editUserAge);
        editUserWeight = findViewById(R.id.editUserWeight);
        editUserHeight = findViewById(R.id.editUserHeight);
        editUserSex = findViewById(R.id.editUserSex);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        backToHomeButton = findViewById(R.id.backToHomeButton);
        logoutButton = findViewById(R.id.logoutButton);
        generateMealPlanButton = findViewById(R.id.generateMealPlanButton);
        profileImage = findViewById(R.id.profileImage);
        userReportMessage = findViewById(R.id.userReportMessage);
        userBMI = findViewById(R.id.userBMI);
        userCalories = findViewById(R.id.userCalories);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        ArrayAdapter<CharSequence> sexAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_sex, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editUserSex.setAdapter(sexAdapter);

        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);
            loadUserProfile();
        }

        saveProfileButton.setOnClickListener(v -> saveUserProfile());
        backToHomeButton.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, HomePageActivity.class)));
        logoutButton.setOnClickListener(v -> logoutUser());
        generateMealPlanButton.setOnClickListener(v -> generateMealPlan());

        loadRecipesFromDatabase();
    }

    public void loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    Long age = dataSnapshot.child("age").getValue(Long.class);
                    Long weight = dataSnapshot.child("weight").getValue(Long.class);
                    Long height = dataSnapshot.child("height").getValue(Long.class);
                    String sex = dataSnapshot.child("sex").getValue(String.class);

                    if (name != null) editUserName.setText(name);
                    if (age != null) editUserAge.setText(String.valueOf(age));
                    if (weight != null) editUserWeight.setText(String.valueOf(weight));
                    if (height != null) editUserHeight.setText(String.valueOf(height));
                    if (sex != null) {
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) editUserSex.getAdapter();
                        int position = adapter.getPosition(sex);
                        editUserSex.setSelection(position);
                    }

                    if (age != null && weight != null && height != null && sex != null && !age.equals(0L) && !weight.equals(0L) && !height.equals(0L)) {
                        userReportMessage.setVisibility(View.GONE);
                        calculateAndDisplayUserReport(age, weight, height, sex);
                        generateMealPlanButton.setVisibility(View.VISIBLE);
                    } else {
                        userReportMessage.setVisibility(View.VISIBLE);
                        userBMI.setVisibility(View.GONE);
                        userCalories.setVisibility(View.GONE);
                        generateMealPlanButton.setVisibility(View.GONE);
                    }
                } else {
                    userReportMessage.setVisibility(View.VISIBLE);
                    userBMI.setVisibility(View.GONE);
                    userCalories.setVisibility(View.GONE);
                    generateMealPlanButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveUserProfile() {
        String name = editUserName.getText().toString().trim();
        String ageStr = editUserAge.getText().toString().trim();
        String weightStr = editUserWeight.getText().toString().trim();
        String heightStr = editUserHeight.getText().toString().trim();
        String sex = editUserSex.getSelectedItem().toString();

        if (name.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || sex.isEmpty()) {
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
        userProfile.put("sex", sex);

        databaseReference.updateChildren(userProfile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                if (!age.equals(0L) && !weight.equals(0L) && !height.equals(0L) && sex != null) {
                    userReportMessage.setVisibility(View.GONE);
                    calculateAndDisplayUserReport(age, weight, height, sex);
                    generateMealPlanButton.setVisibility(View.VISIBLE);
                } else {
                    userReportMessage.setVisibility(View.VISIBLE);
                    userBMI.setVisibility(View.GONE);
                    userCalories.setVisibility(View.GONE);
                    generateMealPlanButton.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void calculateAndDisplayUserReport(Long age, Long weight, Long height, String sex) {
        double bmi = weight / Math.pow(height / 100.0, 2);
        int dailyCalories = calculateDailyCalories(age, weight, height, sex);

        userBMI.setText(String.format("BMI: %.2f", bmi));
        userCalories.setText(String.format("Calorii necesare pe zi: %d", dailyCalories));

        SharedPreferences preferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("dailyCaloricNeed", dailyCalories);
        editor.apply();

        userBMI.setVisibility(View.VISIBLE);
        userCalories.setVisibility(View.VISIBLE);
    }

    public int calculateDailyCalories(Long age, Long weight, Long height, String sex) {
        if (sex == null) {
            return 0;
        }

        //Mifflin-St Jeor
        if (sex.equals("Masculin")) {
            return (int) (10 * weight + 6.25 * height - 5 * age + 5);
        } else if (sex.equals("Feminin")) {
            return (int) (10 * weight + 6.25 * height - 5 * age - 161);
        } else {
            return 0;
        }
    }

    public void loadRecipesFromDatabase() {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes");
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateMealPlan() {
        Intent intent = new Intent(ProfileActivity.this, MealPlanActivity.class);
        intent.putExtra("recipeList", (Serializable) recipeList);
        intent.putExtra("dailyCalories", Integer.parseInt(userCalories.getText().toString().replaceAll("[^0-9]", "")));
        startActivity(intent);
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}
