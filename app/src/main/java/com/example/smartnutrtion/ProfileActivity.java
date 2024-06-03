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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    private EditText editUserName;
    private EditText editUserAge;
    private EditText editUserWeight;
    private EditText editUserHeight;
    private Spinner editUserGoal;
    private Spinner editUserSex;
    private Button saveProfileButton;
    private Button backToHomeButton;
    private Button logoutButton;
    private Button generateMealPlanButton;
    private ImageView profileImage;
    private TextView userReportMessage;
    private TextView userBMI;
    private TextView userCalories;

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
        editUserGoal = findViewById(R.id.editUserGoal);
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

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_goals, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editUserGoal.setAdapter(goalAdapter);

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

    private void loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    Long age = dataSnapshot.child("age").getValue(Long.class);
                    Long weight = dataSnapshot.child("weight").getValue(Long.class);
                    Long height = dataSnapshot.child("height").getValue(Long.class);
                    String goal = dataSnapshot.child("goal").getValue(String.class);
                    String sex = dataSnapshot.child("sex").getValue(String.class);

                    if (name != null) editUserName.setText(name);
                    if (age != null) editUserAge.setText(String.valueOf(age));
                    if (weight != null) editUserWeight.setText(String.valueOf(weight));
                    if (height != null) editUserHeight.setText(String.valueOf(height));
                    if (goal != null) {
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) editUserGoal.getAdapter();
                        int position = adapter.getPosition(goal);
                        editUserGoal.setSelection(position);
                    }
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

    private void saveUserProfile() {
        String name = editUserName.getText().toString().trim();
        String ageStr = editUserAge.getText().toString().trim();
        String weightStr = editUserWeight.getText().toString().trim();
        String heightStr = editUserHeight.getText().toString().trim();
        String goal = editUserGoal.getSelectedItem().toString();
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
        userProfile.put("goal", goal);
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



    private void calculateAndDisplayUserReport(Long age, Long weight, Long height, String sex) {
        double bmi = weight / Math.pow(height / 100.0, 2);
        int dailyCalories = calculateDailyCalories(age, weight, height, sex);

        userBMI.setText(String.format("BMI: %.2f", bmi));
        userCalories.setText(String.format("Calorii necesare pe zi: %d", dailyCalories));

        userBMI.setVisibility(View.VISIBLE);
        userCalories.setVisibility(View.VISIBLE);
    }

    private int calculateDailyCalories(Long age, Long weight, Long height, String sex) {
        if (sex == null) {
            return 0;
        }

            // Basic estimation using Mifflin-St Jeor Equation
        if (sex.equals("Masculin")) {
            return (int) (10 * weight + 6.25 * height - 5 * age + 5); // for males
        } else if (sex.equals("Feminin")) {
            return (int) (10 * weight + 6.25 * height - 5 * age - 161); // for females
        } else {
            return 0;
        }
    }

    private void loadRecipesFromDatabase() {
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
        List<Recipe> mealPlan = getRandomMealPlan();
        showMealPlanDialog(mealPlan);
    }

    private List<Recipe> getRandomMealPlan() {
        List<Recipe> mealPlan = new ArrayList<>();
        Random random = new Random();
        int numOfMeals = random.nextInt(3) + 3; // 3 to 5 meals

        for (int i = 0; i < numOfMeals; i++) {
            int randomIndex = random.nextInt(recipeList.size());
            mealPlan.add(recipeList.get(randomIndex));
        }

        return mealPlan;
    }

    private void showMealPlanDialog(List<Recipe> mealPlan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Plan Alimentar");

        StringBuilder mealPlanText = new StringBuilder();
        for (Recipe recipe : mealPlan) {
            mealPlanText.append(recipe.getName()).append(" - ").append(recipe.getCalories()).append(" kCal\n");
        }

        builder.setMessage(mealPlanText.toString());

        builder.setPositiveButton("Regenerează", (dialog, which) -> {
            List<Recipe> newMealPlan = getRandomMealPlan();
            showMealPlanDialog(newMealPlan);
        });

        builder.setNegativeButton("Închide", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}
