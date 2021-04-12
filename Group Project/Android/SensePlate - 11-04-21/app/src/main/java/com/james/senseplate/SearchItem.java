package com.james.senseplate;

public class SearchItem {
    public String id, foodItem, calories, protein, fat, carbs;

    SearchItem(String id, String foodItem, String calories, String protein, String fat, String carbs) {
        this.id = id;
        this.foodItem = foodItem;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public String getCalories() {
        return calories;
    }

    public String getProtein() {
        return protein;
    }

    public String getFat() {
        return fat;
    }

    public String getCarbs() {
        return carbs;
    }
}