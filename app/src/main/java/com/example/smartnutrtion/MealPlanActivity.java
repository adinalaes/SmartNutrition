package com.example.smartnutrtion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MealPlanActivity extends AppCompatActivity {

    private List<Recipe> recipeList = new ArrayList<>();
    private int dailyCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        LinearLayout mealPlanLayout = findViewById(R.id.mealPlanLayout);
        Button backToProfileButton = findViewById(R.id.backToProfileButton);
        Button regenerateButton = new Button(this);

        Intent intent = getIntent();
        recipeList = (List<Recipe>) intent.getSerializableExtra("recipeList");
        dailyCalories = intent.getIntExtra("dailyCalories", 0);

        generateWeeklyMealPlan(mealPlanLayout);

        backToProfileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(MealPlanActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
            finish();
        });

        regenerateButton.setText("REGENEREAZĂ PLANUL");
        regenerateButton.setBackgroundColor(getResources().getColor(R.color.purple_700));
        regenerateButton.setTextColor(getResources().getColor(android.R.color.white));
        regenerateButton.setPadding(16, 16, 16, 16);
        regenerateButton.setOnClickListener(v -> {
            mealPlanLayout.removeAllViews();
            mealPlanLayout.addView(backToProfileButton);
            generateWeeklyMealPlan(mealPlanLayout);
        });

        mealPlanLayout.addView(regenerateButton);
    }

    public void generateWeeklyMealPlan(LinearLayout mealPlanLayout) {
        for (int day = 1; day <= 7; day++) {
            List<Recipe> mealPlan = getRandomMealPlan(dailyCalories);

            TextView dayTitle = new TextView(this);
            dayTitle.setText(String.format("Ziua %d", day));
            dayTitle.setTextSize(20);
            dayTitle.setPadding(8, 16, 8, 8);
            mealPlanLayout.addView(dayTitle);

            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setPadding(16, 16, 16, 16);
            dayLayout.setBackground(getResources().getDrawable(R.drawable.meal_plan_item_background));

            double totalCalories = 0;

            for (Recipe recipe : mealPlan) {
                View itemView = getLayoutInflater().inflate(R.layout.meal_plan_item, null);

                ImageView recipeImage = itemView.findViewById(R.id.recipeImage);
                TextView recipeName = itemView.findViewById(R.id.recipeName);
                TextView recipeCalories = itemView.findViewById(R.id.recipeCalories);

                Picasso.get().load(recipe.getImageUrl()).into(recipeImage);
                recipeName.setText(recipe.getName());

                double caloriesPerPortion = recipe.getCalories() * recipe.getPortionSize() / 100;
                totalCalories += caloriesPerPortion;
                recipeCalories.setText(String.format(Locale.getDefault(), "%.2f kCal", caloriesPerPortion));

                itemView.setOnClickListener(v -> {
                    Intent detailIntent = new Intent(MealPlanActivity.this, RecipeDetailActivity.class);
                    detailIntent.putExtra("recipe", recipe);
                    startActivity(detailIntent);
                });

                dayLayout.addView(itemView);
            }

            TextView totalCaloriesTextView = new TextView(this);
            totalCaloriesTextView.setText(String.format(Locale.getDefault(), "Total calorii: %.2f kCal", totalCalories));
            totalCaloriesTextView.setTextSize(18);
            totalCaloriesTextView.setPadding(8, 16, 8, 16);
            totalCaloriesTextView.setTextColor(getResources().getColor(android.R.color.black));
            dayLayout.addView(totalCaloriesTextView);

            mealPlanLayout.addView(dayLayout);
        }
    }

    public List<Recipe> getRandomMealPlan(int dailyCalories) {
        List<Recipe> mealPlan = new ArrayList<>();
        List<Recipe> remainingRecipes = new ArrayList<>(recipeList);
        Random random = new Random();
        double totalCalories = 0;
        int minCalories = dailyCalories - 100;
        int maxCalories = dailyCalories + 100;

        while (!remainingRecipes.isEmpty()) {
            int randomIndex = random.nextInt(remainingRecipes.size());
            Recipe recipe = remainingRecipes.get(randomIndex);
            double caloriesPerPortion = recipe.getCalories() * recipe.getPortionSize() / 100;

            if (totalCalories + caloriesPerPortion <= maxCalories) {
                mealPlan.add(recipe);
                totalCalories += caloriesPerPortion;
                remainingRecipes.remove(randomIndex);
            } else {
                remainingRecipes.remove(randomIndex);
            }

            if (totalCalories >= minCalories && totalCalories <= maxCalories) {
                break;
            }
        }

        return mealPlan;
    }
}
