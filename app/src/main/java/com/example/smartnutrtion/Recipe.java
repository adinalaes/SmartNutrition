package com.example.smartnutrtion;

public class Recipe {
    private String name;
    private String imageUrl;
    private double calories;

    // Constructor fără argumente necesar pentru Firebase
    public Recipe() {
    }

    // Constructor cu argumente
    public Recipe(String name, String imageUrl, double calories) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.calories = calories;
    }

    // Getteri
    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getCalories() {
        return calories;
    }

    // Setteri
    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }
}

