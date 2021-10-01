package kent.group8.senseplateandroid;

public class SearchItem {
    public String id, foodItem, amount, brand, calories, protein, fat, carbs;

    SearchItem(String id, String foodItem, String amount, String brand, String calories, String protein, String fat, String carbs) {
        this.id = id;
        this.foodItem = foodItem;
        this.amount = amount;
        this.brand = brand;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    public String getFoodItem() {
        return foodItem;
    }

    public String getAmount() {
        return amount;
    }

    public String getBrand() {
        return brand;
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