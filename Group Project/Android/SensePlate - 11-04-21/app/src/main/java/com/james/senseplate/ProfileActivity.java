package com.james.senseplate;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {
    private List<ProfileStorage> profileList;
    private RecyclerView profileRecyclerView;
    private DatabaseSQLite myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myDb = new DatabaseSQLite(this);

        profileRecyclerView = (RecyclerView) findViewById(R.id.profileRecyclerView);
        GridLayoutManager llm = new GridLayoutManager(getApplicationContext(), 1);
        profileRecyclerView.setLayoutManager(llm);
        profileRecyclerView.setHasFixedSize(true);

        getProfile();
    }

    public void getProfile() {
        profileList = new ArrayList<>();
        //get foods for the given day from database
        Cursor cursor = myDb.getProfile();
        if (cursor.moveToFirst()) {
            do {
                String gender = cursor.getString(1);
                int age = cursor.getInt(2);
                Double height = cursor.getDouble(3);
                Double weight = cursor.getDouble(4);
                String activity = cursor.getString(6);
                String goal = cursor.getString(7);
                Double calorieGoal = cursor.getDouble(8);

                /*Mifflin-St Jeor Equation:
                For men:
                BMR = 10W + 6.25H - 5A + 5
                For women:
                BMR = 10W + 6.25H - 5A - 161

                no excercise + 340
                moderate + 750
                active + 900

                goal
                lose - 250
                maintain
                gain + 250
                */
                if(!gender.isEmpty() && age != 0 && height != 0 && weight != 0 && !activity.isEmpty() && !goal.isEmpty()) {
                    calorieGoal = 0.0;
                    if(activity.equals("Inactive")){
                        calorieGoal += 340;
                    }
                    else if(activity.equals("Moderately active")) {
                        calorieGoal += 750;
                    }
                    else if(activity.equals("Active")){
                        calorieGoal += 900;
                    }

                    if(goal.equals("Lose weight")){
                        calorieGoal -= 250;
                    }
                    else if(goal.equals("Gain weight")){
                        calorieGoal += 250;
                    }

                    if(gender.equals("Male")) {
                        calorieGoal += (int) ((10 * weight) + (6.25 * height) - (5 * age) + 5);
                        myDb.updateProfileSingle("Calorie Target", String.valueOf(calorieGoal));
                    }
                    else if(gender.equals("Female")) {
                        calorieGoal += (int) ((10 * weight) + (6.25 * height) - (5 * age) - 161);
                        myDb.updateProfileSingle("Calorie Target", String.valueOf(calorieGoal));
                    }
                }



                profileList.add(new ProfileStorage("Gender", gender, ""));
                profileList.add(new ProfileStorage("Age", String.valueOf(age), ""));
                profileList.add(new ProfileStorage("Height", String.valueOf(height), "cm"));
                profileList.add(new ProfileStorage("Weight", String.valueOf(weight), "kg"));
                profileList.add(new ProfileStorage("BMI",  String.valueOf(height), String.valueOf(weight))); //pass weight and height to calculate BMI using value and units
                profileList.add(new ProfileStorage("Activity", activity, ""));
                profileList.add(new ProfileStorage("Goal", goal, ""));
                profileList.add(new ProfileStorage("Calorie Target", String.valueOf(calorieGoal), ""));
            } while (cursor.moveToNext());
        }


        if(profileList.isEmpty()) {
            //create empty profile
            myDb.insertProfileDefault();
            getProfile();
        }
        initializeAdapter();
    }

    private void initializeAdapter() {
        ProfileAdapter profileAdapter = new ProfileAdapter(profileList, this);
        profileRecyclerView.setAdapter(profileAdapter);
    }

    @Override
    int getContentViewId() {
        return R.layout.profileactivity_main;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_profile;
    }
}