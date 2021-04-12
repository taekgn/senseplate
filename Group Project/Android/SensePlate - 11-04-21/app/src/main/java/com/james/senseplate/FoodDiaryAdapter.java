package com.james.senseplate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class FoodDiaryAdapter extends RecyclerView.Adapter<FoodDiaryAdapter.ViewHolder> implements BackgroundWorker.TaskListener {
    private Context context;
    private DatabaseSQLite myDb;
    private String mealType;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cv;
        private TextView addFoodView, caloriesView;
        private ImageView plusView;
        public View view;

        ViewHolder(View v) {
            super(v);
            cv = (CardView) itemView.findViewById(R.id.cv);
            addFoodView = (TextView)itemView.findViewById(R.id.tvAddFood);
            caloriesView = (TextView)itemView.findViewById(R.id.tvCalories);
            plusView = (ImageView)itemView.findViewById(R.id.imagePlus);
            view = v;
        }
    }

    List<FoodDiaryStorage> foodDiary;

    FoodDiaryAdapter(List<FoodDiaryStorage> foodDiary, Context context, String mealType) {
        this.foodDiary = foodDiary;
        this.context = context;
        this.mealType = mealType;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_diary, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final FoodDiaryStorage selected = foodDiary.get(i);

        viewHolder.plusView.setVisibility(View.GONE);
        viewHolder.addFoodView.setText(selected.food);
        viewHolder.caloriesView.setText(selected.calories);

        if(selected.foodID.isEmpty()) {
            viewHolder.plusView.setVisibility(View.VISIBLE);
            viewHolder.addFoodView.setText(selected.food);
            viewHolder.caloriesView.setText(selected.calories);
        }

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if add food button is pressed
                if(selected.food.equals("Add Food")) {
                    StoreInfo store = new StoreInfo();
                    store.setMealType(mealType);
                    store.setDate(selected.date);
                    store.setTime(selected.time);
                    //opens search activity
                    context.startActivity(new Intent(context, SearchActivity.class));
                }
                else {
                    //used to remove food item if pressed
                    StoreInfo store = new StoreInfo();
                    store.setMealType(mealType);
                    store.setDate(selected.date);
                    store.setTime(selected.time);
                    alertbox(selected.id, selected.food, selected.calories);
                }
            }
        });
    }

    //shows alertbox for option to remove food item
    public void alertbox(String id, String food, String calories) {
        myDb = new DatabaseSQLite(context);
        final String finalID = id;
        new AlertDialog.Builder(context)
                .setTitle("Remove Food")
                .setMessage("Are you sure you want to remove " + food + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        myDb.removeFoodDiary(finalID);
                        //reopens the activity to refresh it, without an animation
                        Intent i = new Intent(context, DiaryActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(i);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
            }}).show();
    }

    public void onTaskCompleted(String result) {
    }

    @Override
    public int getItemCount () {
        return foodDiary.size();
    }
}
