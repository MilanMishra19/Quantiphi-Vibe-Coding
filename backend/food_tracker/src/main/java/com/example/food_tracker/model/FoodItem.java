package com.example.food_tracker.model;

public class FoodItem {
    public long id;
    public String name;
    public double grams;
    public double calories;
    public double protein;
    public double carbs;
    public double fats;

    public FoodItem(long id, String name, double grams, double calories, double protein, double carbs, double fats) {
        this.id = id;
        this.name = name;
        this.grams = grams;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
    }
}