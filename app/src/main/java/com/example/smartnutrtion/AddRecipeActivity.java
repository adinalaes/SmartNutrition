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

    private EditText editTextRecipeName, editTextIngredients, editTextImageUrl, editTextCalories, editTextFats, editTextSaturatedFats, editTextUnsaturatedFats, editTextCarbohydrates, editTextProtein, editTextFiber, editTextSugar, editTextSalt, editTextDescription;
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

    private void saveRecipeToFirebase() {
        String recipeName = editTextRecipeName.getText().toString().trim();
        List<String> ingredients = Arrays.asList(editTextIngredients.getText().toString().trim().split(","));
        String imageUrl = editTextImageUrl.getText().toString().trim();
        double calories = Double.parseDouble(editTextCalories.getText().toString().trim());
        String description = editTextDescription.getText().toString().trim();

        Map<String, Double> nutritionalValues = new HashMap<>();
        nutritionalValues.put("fats", Double.parseDouble(editTextFats.getText().toString().trim()));
        nutritionalValues.put("saturatedFats", Double.parseDouble(editTextSaturatedFats.getText().toString().trim()));
        nutritionalValues.put("unsaturatedFats", Double.parseDouble(editTextUnsaturatedFats.getText().toString().trim()));
        nutritionalValues.put("carbohydrates", Double.parseDouble(editTextCarbohydrates.getText().toString().trim()));
        nutritionalValues.put("protein", Double.parseDouble(editTextProtein.getText().toString().trim()));
        nutritionalValues.put("fiber", Double.parseDouble(editTextFiber.getText().toString().trim()));
        nutritionalValues.put("sugar", Double.parseDouble(editTextSugar.getText().toString().trim()));
        nutritionalValues.put("salt", Double.parseDouble(editTextSalt.getText().toString().trim()));

        Recipe recipe = new Recipe(recipeName, imageUrl, calories, ingredients, nutritionalValues, description);

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
