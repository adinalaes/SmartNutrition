package com.example.smartnutrtion;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JournalActivity extends AppCompatActivity {
    private ProgressBar progressBarCalories;
    private TextView textViewCalories;
    private TextView textViewDate;
    private RecyclerView recyclerViewMeals;
    private MealAdapter mealAdapter;
    private List<Recipe> mealList;
    private double totalCaloriesConsumed = 0;
    private double dailyCaloricNeed = 1800;
    private String selectedDate;
    private DatabaseReference userJournalRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        progressBarCalories = findViewById(R.id.progressBarCalories);
        textViewCalories = findViewById(R.id.textViewCalories);
        textViewDate = findViewById(R.id.textViewDate);
        recyclerViewMeals = findViewById(R.id.recyclerViewMeals);
        ImageButton calendarButton = findViewById(R.id.calendarButton);
        Button addMealButton = findViewById(R.id.addMealButton);
        Button backButton = findViewById(R.id.backButton);

        mealList = new ArrayList<>();
        mealAdapter = new MealAdapter(this, mealList);
        recyclerViewMeals.setAdapter(mealAdapter);
        recyclerViewMeals.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userJournalRef = FirebaseDatabase.getInstance().getReference("journals").child(currentUser.getUid());
        }

        SharedPreferences preferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        dailyCaloricNeed = preferences.getInt("dailyCaloricNeed", 1800);

        Calendar calendar = Calendar.getInstance();
        selectedDate = getFormattedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        textViewDate.setText("Jurnal pentru ziua: " + selectedDate);
        loadJournalForDate(selectedDate);

        addMealButton.setOnClickListener(v -> {
            Intent intent = new Intent(JournalActivity.this, RecipeSelectionActivity.class);
            startActivityForResult(intent, 1);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(JournalActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });

        calendarButton.setOnClickListener(v -> showDatePickerDialog());

        updateProgressBar();
    }

    public void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = getFormattedDate(year1, month1, dayOfMonth);
                    textViewDate.setText("Jurnal pentru ziua: " + selectedDate);
                    loadJournalForDate(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    public void loadJournalForDate(String date) {
        if (userJournalRef != null) {
            userJournalRef.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mealList.clear();
                    totalCaloriesConsumed = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Recipe recipe = snapshot.getValue(Recipe.class);
                        if (recipe != null) {
                            mealList.add(recipe);
                            totalCaloriesConsumed += recipe.getPortionSize() * 0.01 * recipe.getCalories();
                        }
                    }
                    mealAdapter.notifyDataSetChanged();
                    updateProgressBar();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    public void updateProgressBar() {
        int progress = (int) ((totalCaloriesConsumed / dailyCaloricNeed) * 100);
        progressBarCalories.setProgress(progress);
        textViewCalories.setText(String.format("%.2f / %.0f kCal", totalCaloriesConsumed, dailyCaloricNeed));
    }

    public String getFormattedDate(int year, int month, int day) {
        return day + "/" + (month + 1) + "/" + year;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Recipe selectedRecipe = (Recipe) data.getSerializableExtra("selectedRecipe");
            if (selectedRecipe != null) {
                mealList.add(selectedRecipe);
                totalCaloriesConsumed += selectedRecipe.getPortionSize() * 0.01 * selectedRecipe.getCalories();
                mealAdapter.notifyDataSetChanged();
                updateProgressBar();
                saveMealForDate(selectedRecipe, selectedDate);
            }
        }
    }

    public void saveMealForDate(Recipe selectedRecipe, String date) {
        if (userJournalRef != null) {
            userJournalRef.child(date).push().setValue(selectedRecipe);
        }
    }
}
