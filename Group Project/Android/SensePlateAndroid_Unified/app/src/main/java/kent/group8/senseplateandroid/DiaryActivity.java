package kent.group8.senseplateandroid;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiaryActivity extends BaseActivity {

    private List<FoodDiaryStorage> breakfastList, lunchList, dinnerList, snacksList;
    private RecyclerView breakfastRecyclerView, lunchRecyclerView, dinnerRecyclerView, snacksRecyclerView;
    private DatabaseSQLite myDb;
    private TextView tvCalories, tvCaloriesText, tvBreakfastCalories, tvLunchCalories, tvDinnerCalories, tvSnacksCalories, tvDate;
    private ImageButton buttonPrev, buttonNext;
    private int dateCounter = 0;
    private SimpleDateFormat sdf, sdf2, sdf3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myDb = new DatabaseSQLite(this);

        tvCalories = (TextView) findViewById(R.id.tvCalories);
        tvCaloriesText = (TextView) findViewById(R.id.tvCaloriesText);
        tvBreakfastCalories = (TextView) findViewById(R.id.tvBreakfastCalories);
        tvLunchCalories = (TextView) findViewById(R.id.tvLunchCalories);
        tvDinnerCalories = (TextView) findViewById(R.id.tvDinnerCalories);
        tvSnacksCalories = (TextView) findViewById(R.id.tvSnacksCalories);
        //multiple recyclerviews used to have individual ones for different meals
        breakfastRecyclerView = (RecyclerView) findViewById(R.id.breakfastRecyclerView);
        lunchRecyclerView = (RecyclerView) findViewById(R.id.lunchRecyclerView);
        dinnerRecyclerView = (RecyclerView) findViewById(R.id.dinnerRecyclerView);
        snacksRecyclerView = (RecyclerView) findViewById(R.id.snacksRecyclerView);
        GridLayoutManager llm = new GridLayoutManager(getApplicationContext(), 1);
        breakfastRecyclerView.setLayoutManager(llm);
        GridLayoutManager llm2 = new GridLayoutManager(getApplicationContext(), 1);
        lunchRecyclerView.setLayoutManager(llm2);
        GridLayoutManager llm3 = new GridLayoutManager(getApplicationContext(), 1);
        dinnerRecyclerView.setLayoutManager(llm3);
        GridLayoutManager llm4 = new GridLayoutManager(getApplicationContext(), 1);
        snacksRecyclerView.setLayoutManager(llm4);
        breakfastRecyclerView.setHasFixedSize(true);
        lunchRecyclerView.setHasFixedSize(true);
        dinnerRecyclerView.setHasFixedSize(true);
        snacksRecyclerView.setHasFixedSize(true);

        tvDate = (TextView) findViewById(R.id.tvDate);
        buttonPrev = (ImageButton) findViewById(R.id.buttonPrevious);
        buttonNext = (ImageButton) findViewById(R.id.buttonNext);

        //button to go to previous day
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dateCounter--;
                getFoodDiary();
            }
        });
        //button to go to next day
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dateCounter++;
                getFoodDiary();
            }
        });

        getFoodDiary();
    }

    public void getFoodDiary() {
        double totalCalories = 0, breakfastCalories = 0, lunchCalories = 0, dinnerCalories = 0, snacksCalories = 0;

        sdf = new SimpleDateFormat("E, MMM d");
        sdf2 = new SimpleDateFormat("yyyy/MM/dd");
        sdf3 = new SimpleDateFormat("HH:mm:ss");

        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, dateCounter);
        dt = c.getTime();
        String todayDateDisplay = sdf.format(dt);
        String todayDate = sdf2.format(dt);
        String todayTime = sdf3.format(dt);
        //get date and time from StoreInfo
        StoreInfo store = new StoreInfo();
        String savedDate = store.getDate();
        String savedTime = store.getDate();

        String dateToUse = "", timeToUse = "";
        //check which date to use
        if(savedDate == null) {
            dateToUse = todayDate;
            timeToUse = todayTime;
        }
        else {
            try {
                dateToUse = savedDate;
                timeToUse = savedTime;
                //put values to dates to compare
                Date date1 = sdf2.parse(savedDate);
                Date date2 = sdf2.parse(sdf2.format(dt));

                //calculate difference between two dates, given in days
                long diffInMillies = Math.abs(date1.getTime() - date2.getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                //make difference a negative value if it is before todays date
                if(date1.before(date2)) {
                    diff *= -1;
                }
                //set dateCounter to the value off diff to set the correct day on diary
                dateCounter = (int) diff;
            }
            catch (ParseException e) {
            }
        }

        //set the textView for the date
        if(dateCounter == -1) {
            tvDate.setText("Yesterday");
        } else if(dateCounter == 0) {
            tvDate.setText("Today");
        } else if(dateCounter == 1) {
            tvDate.setText("Tomorrow");
        } else {
            Date dt2 = new Date();
            Calendar c2 = Calendar.getInstance();
            c2.setTime(dt2);
            c2.add(Calendar.DATE, dateCounter);
            dt2 = c2.getTime();
            tvDate.setText(sdf.format(dt2));
        }

        breakfastList = new ArrayList<>();
        lunchList = new ArrayList<>();
        dinnerList = new ArrayList<>();
        snacksList = new ArrayList<>();
        //get foods for the given day from database
        Cursor cursor = myDb.getFoodDiary("WHERE date = '" + dateToUse + "'");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String foodID = cursor.getString(1);
                String food = cursor.getString(2);
                String calories = cursor.getString(3);
                String mealType = cursor.getString(7);
                String date2 = cursor.getString(8);
                String time2 = cursor.getString(9);
                if(mealType.equals("Breakfast")) {
                    breakfastList.add(new FoodDiaryStorage(id, foodID, food, calories, mealType, date2, time2));
                    breakfastCalories += Double.valueOf(calories);
                }
                if(mealType.equals("Lunch")) {
                    lunchList.add(new FoodDiaryStorage(id, foodID, food, calories, mealType, date2, time2));
                    lunchCalories += Double.valueOf(calories);
                }
                if(mealType.equals("Dinner")) {
                    dinnerList.add(new FoodDiaryStorage(id, foodID, food, calories, mealType, date2, time2));
                    dinnerCalories += Double.valueOf(calories);
                }
                if(mealType.equals("Snacks")) {
                    snacksList.add(new FoodDiaryStorage(id, foodID, food, calories, mealType, date2, time2));
                    snacksCalories += Double.valueOf(calories);
                }
            } while (cursor.moveToNext());
        }
        //calculate total calories for that day
        totalCalories = breakfastCalories + lunchCalories + dinnerCalories + snacksCalories;

        Double caloriesTarget = 0.00;
        Cursor cursor2 = myDb.getCalorieTarget();
        if (cursor2.moveToFirst()) {
            do {
                caloriesTarget = cursor2.getDouble(0);

            } while (cursor.moveToNext());
        }
        if(caloriesTarget != 0.0) {
            tvCaloriesText.setVisibility(View.GONE);
            tvCalories.setText(String.format("%.0f", caloriesTarget) + "        -          " + String.format("%.0f", totalCalories) + "         =          " + String.format("%.0f", (caloriesTarget - totalCalories)) + "      ");
        }
        else {
            tvCaloriesText.setVisibility(View.VISIBLE);
            tvCalories.setText(String.valueOf(totalCalories));
        }
        tvBreakfastCalories.setText(String.valueOf(breakfastCalories));
        tvLunchCalories.setText(String.valueOf(lunchCalories));
        tvDinnerCalories.setText(String.valueOf(dinnerCalories));
        tvSnacksCalories.setText(String.valueOf(snacksCalories));

        //add to the bottom of recyclerview to allow user to add items
        breakfastList.add(new FoodDiaryStorage("", "", "Add Food", "", "", dateToUse, timeToUse));
        lunchList.add(new FoodDiaryStorage("","", "Add Food", "", "", dateToUse, timeToUse));
        dinnerList.add(new FoodDiaryStorage("","", "Add Food", "", "", dateToUse, timeToUse));
        snacksList.add(new FoodDiaryStorage("","", "Add Food", "", "", dateToUse, timeToUse));
        //clear date and time values
        store.setDate(null);
        store.setTime(null);

        initializeAdapter();
    }

    private void initializeAdapter() {
        FoodDiaryAdapter breakfastAdapter = new FoodDiaryAdapter(breakfastList, this, "Breakfast");
        breakfastRecyclerView.setAdapter(breakfastAdapter);
        FoodDiaryAdapter lunchAdapter = new FoodDiaryAdapter(lunchList, this, "Lunch");
        lunchRecyclerView.setAdapter(lunchAdapter);
        FoodDiaryAdapter dinnerAdapter = new FoodDiaryAdapter(dinnerList, this, "Dinner");
        dinnerRecyclerView.setAdapter(dinnerAdapter);
        FoodDiaryAdapter snacksAdapter = new FoodDiaryAdapter(snacksList, this, "Snacks");
        snacksRecyclerView.setAdapter(snacksAdapter);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_diary;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_diary;
    }
}