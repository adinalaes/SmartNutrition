package com.example.smartnutrtion;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Recipe implements Serializable {
    private String name;
    private String imageUrl;
    private double calories;

    private List<String> ingredients;

    private Map<String, Double> nutritionalValues;

    public Recipe(){

    }

    public Recipe(String name, String imageUrl, double calories, List<String> ingredients, Map<String, Double> nutritionalValues) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.calories = calories;
        this.ingredients = ingredients;
        this.nutritionalValues = nutritionalValues;
    }

    public String getName() {
        return name;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public double getCalories() {
        return calories;
    }
    public List<String> getIngredients() { return ingredients; }
    public Map<String, Double> getNutritionalValues() { return nutritionalValues; }


    public void setName(String name) {
        this.name = name;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public void setCalories(double calories) {
        this.calories = calories;
    }
    public void setIngredients(List<String> ingredients) {this.ingredients = ingredients; }
    public void setNutritionalValues(Map<String, Double> nutritionalValues) {this.nutritionalValues = nutritionalValues; }

}

