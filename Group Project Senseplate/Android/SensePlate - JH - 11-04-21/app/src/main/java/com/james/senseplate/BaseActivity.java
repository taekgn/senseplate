package com.james.senseplate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected BottomNavigationView NavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        NavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        NavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    //Stops the animation for when a user spams the buttons
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final MenuItem t = item;
        NavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {

                int itemId = t.getItemId();
                //Takes the user to the correct activity depending on the button they pressed
                if (itemId == R.id.navigation_diary) {
                    //Opens the AvailableShiftsActivity
                    startActivity(new Intent(BaseActivity.this, DiaryActivity.class));
                } else if (itemId == R.id.navigation_compare) {
                    //Opens the YourShiftsActivity
                    startActivity(new Intent(BaseActivity.this, LineChartActivity.class));
                } else if (itemId == R.id.navigation_profile) {
                    //Opens the TimesheetActivity
                    startActivity(new Intent(BaseActivity.this, ProfileActivity.class));
                }
                BaseActivity.this.finish();
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState(){
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = NavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

    abstract int getContentViewId();

    abstract int getNavigationMenuItemId();

}
