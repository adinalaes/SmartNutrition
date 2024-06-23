package com.example.smartnutrtion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText editTextRecipeName, editTextIngredients, editTextImageUrl, editTextCalories, editTextFats, editTextSaturatedFats, editTextUnsaturatedFats, editTextCarbohydrates, editTextProtein, editTextFiber, editTextSugar, editTextSalt, editTextDescription, editTextPortionSize;
    private Button buttonSave, buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        editTextRecipeName = findViewById(R.id.editTextRecipeName);
        editTextIngredients = findViewById(R.id.editTextIngredients);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        editTextCalories = findViewById(R.id.editTextCalories);
        editTextFats = findViewById(R.id.editTextFats);
        editTextSaturatedFats = findViewById(R.id.editTextSaturatedFats);
        editTextUnsaturatedFats = findViewById(R.id.editTextUnsaturatedFats);
        editTextCarbohydrates = findViewById(R.id.editTextCarbohydrates);
        editTextProtein = findViewById(R.id.editTextProtein);
        editTextFiber = findViewById(R.id.editTextFiber);
        editTextSugar = findViewById(R.id.editTextSugar);
        editTextSalt = findViewById(R.id.editTextSalt);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPortionSize = findViewById(R.id.editTextPortionSize);
        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.buttonBack);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipeToFirebase();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddRecipeActivity.this, HomePageActivity.class));
                finish();
            }
        });
    }

    public void saveRecipeToFirebase() {
        String recipeName = editTextRecipeName.getText().toString().trim();
        String ingredientsText = editTextIngredients.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        String caloriesText = editTextCalories.getText().toString().trim();
        String portionSizeText = editTextPortionSize.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String fatsText = editTextFats.getText().toString().trim();
        String saturatedFatsText = editTextSaturatedFats.getText().toString().trim();
        String unsaturatedFatsText = editTextUnsaturatedFats.getText().toString().trim();
        String carbohydratesText = editTextCarbohydrates.getText().toString().trim();
        String proteinText = editTextProtein.getText().toString().trim();
        String fiberText = editTextFiber.getText().toString().trim();
        String sugarText = editTextSugar.getText().toString().trim();
        String saltText = editTextSalt.getText().toString().trim();

        if (recipeName.isEmpty() || ingredientsText.isEmpty() || caloriesText.isEmpty() || portionSizeText.isEmpty() ||
                description.isEmpty() || fatsText.isEmpty() || saturatedFatsText.isEmpty() ||
                unsaturatedFatsText.isEmpty() || carbohydratesText.isEmpty() || proteinText.isEmpty() ||
                fiberText.isEmpty() || sugarText.isEmpty() || saltText.isEmpty()) {
            Toast.makeText(this, "Este necesară completarea câmpurilor obligatorii!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUrl.isEmpty()) {
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSdK9HIraJG9yY1lNo2KWICOshaKOMOq8qlrw&s";
        }

        List<String> ingredients = Arrays.asList(ingredientsText.split(","));
        double calories = Double.parseDouble(caloriesText);
        double portionSize = Double.parseDouble(portionSizeText);

        Map<String, Double> nutritionalValues = new HashMap<>();
        nutritionalValues.put("fats", Double.parseDouble(fatsText));
        nutritionalValues.put("saturatedFats", Double.parseDouble(saturatedFatsText));
        nutritionalValues.put("unsaturatedFats", Double.parseDouble(unsaturatedFatsText));
        nutritionalValues.put("carbohydrates", Double.parseDouble(carbohydratesText));
        nutritionalValues.put("protein", Double.parseDouble(proteinText));
        nutritionalValues.put("fiber", Double.parseDouble(fiberText));
        nutritionalValues.put("sugar", Double.parseDouble(sugarText));
        nutritionalValues.put("salt", Double.parseDouble(saltText));

        Recipe recipe = new Recipe(recipeName, imageUrl, calories, ingredients, nutritionalValues, description, portionSize);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        databaseReference.push().setValue(recipe).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddRecipeActivity.this, "Rețeta a fost salvată cu succes!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddRecipeActivity.this, "Eroare la salvarea rețetei: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
