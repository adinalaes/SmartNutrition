package com.example.smartnutrtion;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomePageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private ImageButton filterButton;
    private SearchView searchView;
    private Button addRecipeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        searchView = findViewById(R.id.searchView);
        filterButton = findViewById(R.id.filterButton);
        recyclerView = findViewById(R.id.recipesRecyclerView);
        addRecipeButton = findViewById(R.id.addRecipeButton);

        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, recipeList);
        recyclerView.setAdapter(recipeAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        filterButton.setOnClickListener(v -> showFilterDialog());
        addRecipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseRef = database.getReference("recipes");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    recipeList.add(recipe);
                }
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load recipes: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecipesByName(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecipesByName(newText);
                return true;
            }
        });

        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(Color.GRAY);

        searchView.setBackground(new ColorDrawable(Color.WHITE));
        searchView.setPadding(0, 0, 0, 0);
        searchView.setBackgroundResource(R.drawable.search_view_background);
    }

    private void filterRecipesByName(String query) {
        List<Recipe> filteredList = recipeList.stream()
                .filter(recipe -> recipe.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        recipeAdapter.updateList(filteredList);
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filtrează rețetele");

        String[] filterOptions = {
                "Fără filtre", "sub 200 kCal", "200 - 400 kCal", "400 - 600 kCal", "600 - 800 kCal", "peste 800 kCal",
                "Proteice (peste 30% proteine)", "Low Carb (sub 20% carbohidrați)", "Low Fat (sub 20% grăsimi)", "Keto (peste 70% grăsimi)"
        };
        boolean[] checkedItems = new boolean[filterOptions.length];

        builder.setMultiChoiceItems(filterOptions, checkedItems, (dialog, which, isChecked) -> {
            if (which == 0) {
                for (int i = 1; i < checkedItems.length; i++) {
                    checkedItems[i] = false;
                    ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                }
            } else if (isChecked) {
                checkedItems[0] = false;
                ((AlertDialog) dialog).getListView().setItemChecked(0, false);
            }
        });

        builder.setPositiveButton("Aplică", (dialog, which) -> {
            applyFilters(checkedItems);
            dialog.dismiss();
        });

        builder.setNegativeButton("Anulează", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void applyFilters(boolean[] checkedItems) {
        List<Recipe> filteredList = new ArrayList<>(recipeList);

        if (checkedItems[1]) { // sub 200 kCal
            filteredList = filteredList.stream()
                    .filter(recipe -> recipe.getCalories() < 200)
                    .collect(Collectors.toList());
        } else if (checkedItems[2]) { // 200 - 400 kCal
            filteredList = filteredList.stream()
                    .filter(recipe -> recipe.getCalories() >= 200 && recipe.getCalories() < 400)
                    .collect(Collectors.toList());
        } else if (checkedItems[3]) { // 400 - 600 kCal
            filteredList = filteredList.stream()
                    .filter(recipe -> recipe.getCalories() >= 400 && recipe.getCalories() < 600)
                    .collect(Collectors.toList());
        } else if (checkedItems[4]) { // 600 - 800 kCal
            filteredList = filteredList.stream()
                    .filter(recipe -> recipe.getCalories() >= 600 && recipe.getCalories() < 800)
                    .collect(Collectors.toList());
        } else if (checkedItems[5]) { // peste 800 kCal
            filteredList = filteredList.stream()
                    .filter(recipe -> recipe.getCalories() >= 800)
                    .collect(Collectors.toList());
        }

        if (checkedItems[6]) { // Proteice (peste 30% proteine)
            filteredList = filteredList.stream()
                    .filter(recipe -> (recipe.getNutritionalValues().get("protein") / (recipe.getCalories() * 4)) > 0.3)
                    .collect(Collectors.toList());
        } else if (checkedItems[7]) { // Low Carb (sub 20% carbohidrati)
            filteredList = filteredList.stream()
                    .filter(recipe -> (recipe.getNutritionalValues().get("carbohydrates") / (recipe.getCalories() * 4)) < 0.2)
                    .collect(Collectors.toList());
        } else if (checkedItems[8]) { // Low Fat (sub 20% grasimi)
            filteredList = filteredList.stream()
                    .filter(recipe -> (recipe.getNutritionalValues().get("fats") / (recipe.getCalories() * 9)) < 0.2)
                    .collect(Collectors.toList());
        } else if (checkedItems[9]) { // Keto (peste 70% grasimi)
            filteredList = filteredList.stream()
                    .filter(recipe -> (recipe.getNutritionalValues().get("fats") / (recipe.getCalories() * 9)) > 0.7)
                    .collect(Collectors.toList());
        }

        if (checkedItems[0]) {
            filteredList = new ArrayList<>(recipeList);
        }

        recipeAdapter.updateList(filteredList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(HomePageActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
