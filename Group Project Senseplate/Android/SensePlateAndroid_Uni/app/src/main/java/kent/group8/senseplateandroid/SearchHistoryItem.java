package kent.group8.senseplateandroid;

public class SearchHistoryItem {
    public int id;
    public String foodItem;

    SearchHistoryItem(int id, String foodItem) {
        this.id = id;
        this.foodItem = foodItem;
    }

    public String getFoodItem() {
        return foodItem;
    }
}