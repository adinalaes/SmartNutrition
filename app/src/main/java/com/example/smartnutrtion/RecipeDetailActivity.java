package com.example.smartnutrtion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ImageView recipeImage = findViewById(R.id.recipeImage);
        TextView recipeName = findViewById(R.id.recipeName);
        TextView ingredientsList = findViewById(R.id.ingredientsList);
        TextView caloriesLabel = findViewById(R.id.caloriesLabel);
        TextView caloriesValueHeader = findViewById(R.id.caloriesValueHeader);
        TextView carbsValue = findViewById(R.id.carbsValue);
        TextView fiberValue = findViewById(R.id.fiberValue);
        TextView proteinValue = findViewById(R.id.proteinValue);
        TextView saturatedFatsValue = findViewById(R.id.saturatedFatsValue);
        TextView unsaturatedFatsValue = findViewById(R.id.unsaturatedFatsValue);
        TextView sugarValue = findViewById(R.id.sugarValue);
        TextView saltValue = findViewById(R.id.saltValue);
        Button backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        Recipe recipe = (Recipe) intent.getSerializableExtra("recipe");

        recipeName.setText(recipe.getName());
        Picasso.get().load(recipe.getImageUrl()).into(recipeImage);

        StringBuilder ingredientsBuilder = new StringBuilder();
        for (String ingredient : recipe.getIngredients()) {
            ingredientsBuilder.append("• ").append(ingredient).append("\n");
        }
        ingredientsList.setText(ingredientsBuilder.toString().trim());

        String caloriesText = String.format(Locale.getDefault(), "Calorii: %.2f kcal", recipe.getCalories());
        caloriesValueHeader.setText(caloriesText);

        carbsValue.setText(String.format(Locale.getDefault(), "%.2f g", recipe.getNutritionalValues().get("carbohydrates")));
        fiberValue.setText(String.format(Locale.getDefault(), "%.2f g", recipe.getNutritionalValues().get("fiber")));
        proteinValue.setText(String.format(Locale.getDefault(), "%.2f g", recipe.getNutritionalValues().get("protein")));
        saturatedFatsValue.setText(String.format(Locale.getDefault(), "%.2f g", recipe.getNutritionalValues().get("saturatedFats")));
        unsaturatedFatsValue.setText(String.format(Locale.getDefault(), "%.2f g", recipe.getNutritionalValues().get("unsaturatedFats")));
        sugarValue.setText(String.format(Locale.getDefault(), "%.2f g", recipe.getNutritionalValues().get("sugar")));
        saltValue.setText(String.format(Locale.getDefault(), "%.2f g", recipe.getNutritionalValues().get("salt")));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Încheie activitatea curentă și revine la activitatea anterioară
                finish();
            }
        });
    }




}
