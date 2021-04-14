package kent.group8.senseplateandroid;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private List<SearchItem> searchList;
    private Context context;
    private DatabaseSQLite myDb;
    private final int itemLimit = 15;

    class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView foodItemTextView, caloriesTextView, amountTextView, brandTextView;

        SearchViewHolder(View itemView) {
            super(itemView);
            foodItemTextView = itemView.findViewById(R.id.tvFoodItem);
            caloriesTextView = itemView.findViewById(R.id.tvCalories);
            amountTextView = itemView.findViewById(R.id.tvAmount);
            brandTextView = itemView.findViewById(R.id.tvBrand);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDb = new DatabaseSQLite(context);
                    int pos = getAdapterPosition();
                    String foodID = searchList.get(pos).id;
                    String foodItem = searchList.get(pos).foodItem;
                    String amount = searchList.get(pos).amount;
                    String calories = searchList.get(pos).calories;
                    String protein = searchList.get(pos).protein;
                    String fat = searchList.get(pos).fat;
                    String carbs = searchList.get(pos).carbs;
                    Cursor cursor = myDb.findMultipleHistory(foodItem);
                    if (cursor.moveToFirst()) {
                        do {
                            String id = cursor.getString(0);
                            String removeAmount = cursor.getString(1);
                            if(Integer.valueOf(removeAmount) == 1) {
                                myDb.removeMultipleHistory(id);
                            }
                            myDb.insertHistory(foodItem);
                        } while (cursor.moveToNext());
                    }
                    getFood(foodItem, amount, calories, protein, fat, carbs);
                }
            });
        }
    }

    //open foodActivity passing values
    public void getFood(String foodItem, String amount, String calories, String fats, String protein, String carbs) {
        context.startActivity(new Intent(context, FoodActivity.class).putExtra("foodID", foodItem).putExtra("foodItem", foodItem).putExtra("amount", amount).putExtra("calories", calories).putExtra("fat", fats).putExtra("protein", protein).putExtra("carbs", carbs).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    SearchAdapter(List<SearchItem> searchList, Context context) {
        this.searchList = searchList;
        this.context = context;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search,
                parent, false);
        return new SearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchItem currentItem = searchList.get(position);
        holder.foodItemTextView.setText(currentItem.getFoodItem());
 //       holder.brandTextView.setText(currentItem.getBrand());
        holder.brandTextView.setText(currentItem.getAmount() + "g"); //temporary
        holder.caloriesTextView.setText(currentItem.getCalories().substring(0, currentItem.getCalories().indexOf(".")));
    }

    @Override
    public int getItemCount() {
        if(searchList.size() > itemLimit){
            return itemLimit;
        }
        else {
            return searchList.size();
        }
    }
}