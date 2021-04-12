package com.james.senseplate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements BackgroundWorker.TaskListener {
    private SearchAdapter searchAdapter;
    private SearchHistoryAdapter historyAdapter;
    private List<SearchItem> searchList;
    private List<SearchHistoryItem> historyList;
    private RecyclerView historyRecyclerView, searchRecyclerView;
    private DatabaseSQLite myDb;
    private SearchView searchView;
    private TextView recentTV, clearTV, noHistoryTV, noHistoryTV2;
    private ImageView noHistoryIV;

    public SearchActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchactivity_main);
        setTitle("");
        myDb = new DatabaseSQLite(this);
        recentTV = (TextView) findViewById(R.id.textView);
        clearTV = (TextView) findViewById(R.id.textView2);
        noHistoryTV = (TextView) findViewById(R.id.textView3);
        noHistoryTV2 = (TextView) findViewById(R.id.textView4);
        noHistoryIV = (ImageView) findViewById(R.id.nohistory);
        historyRecyclerView = findViewById(R.id.history_rv);
        searchRecyclerView = findViewById(R.id.search_rv);
        searchRecyclerView.setVisibility(View.GONE);
        fillHistoryList();

        //used when item is pressed from searchhistory list in SearchHistoryAdapter
        try {
            Intent intent = getIntent();
            String foodItem = intent.getExtras().getString("foodItem");
            search(foodItem);
            historyRecyclerView.setVisibility(View.GONE);
            recentTV.setVisibility(View.GONE);
            clearTV.setVisibility(View.GONE);
            noHistoryTV.setVisibility(View.GONE);
            noHistoryTV2.setVisibility(View.GONE);
            noHistoryIV.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.VISIBLE);
        }
        catch (NullPointerException e) {
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        try {
            parseSearch(result);
        } catch(JSONException e) {
        }
    }

    //search the api for the user's input
    public void search(String searchedItem) {
        String type = "edamam_api";
        String url = "https://api.edamam.com/api/food-database/v2/parser?ingr=" + searchedItem.replace(" ", "%20") + "&catergoryLabel=food&app_id=a02bcc70&app_key=4e0c1dbdbd5c15af5c8b9501c9432467";
        BackgroundWorker backgroundWorker = new BackgroundWorker(this, this);
        backgroundWorker.execute(type, url);
    }

    //parses json into strings
    public void parseSearch(String result) throws JSONException {
        searchList = new ArrayList<SearchItem>();
        String foodID = "", foodName = "", kcal = "", protein = "", fat = "", carbs = "";
        JSONObject jsonObject = new JSONObject(result);
        //to extract required parts of the json
        JSONArray food = jsonObject.getJSONArray("hints");
        //if the user searches for something that isn't valid
        if(food.length() == 0) {
            Toast.makeText(getApplicationContext(), "Couldn't find item, please try something else", Toast.LENGTH_LONG).show();
        }

        //cycles through the json parsing it to strings
        for (int i = 0; i < food.length(); i++) {
            JSONObject result1 = food.getJSONObject(i);
            JSONObject foodObject = result1.getJSONObject("food");
            foodID = foodObject.getString("foodId");
            foodName = foodObject.getString("label");

            JSONObject nutritionObject = foodObject.getJSONObject("nutrients");
            kcal = nutritionObject.getString("ENERC_KCAL");
            protein = nutritionObject.getString("PROCNT");
            fat = nutritionObject.getString("FAT");
            carbs = nutritionObject.getString("CHOCDF");
            searchList.add(new SearchItem(foodID, WordUtils.capitalizeFully(foodName), kcal, protein, fat, carbs));
        }
        searchRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        searchAdapter = new SearchAdapter(searchList, getApplicationContext());
        searchRecyclerView.setLayoutManager(layoutManager);
        searchRecyclerView.setAdapter(searchAdapter);
    }

    //previously searched by the user
    public void fillHistoryList() {
        historyList = new ArrayList<>();
        myDb = new DatabaseSQLite(getApplicationContext());
        Cursor cursor2 = myDb.getHistory();
        if (cursor2.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor2.getString(0));
                String food = cursor2.getString(1);
                historyList.add(new SearchHistoryItem(id, food));
            } while (cursor2.moveToNext());
        }

        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this);
        historyAdapter = new SearchHistoryAdapter(historyList, getApplicationContext(), new SearchHistoryAdapter.AdapterListener() {
            @Override
            public void iconViewOnClick(View v, int position) {
                String foodItem = historyList.get(position).foodItem;
             //   searchView.setQuery(foodItem, false);
            }
            @Override
            public void iconImageViewOnClick(View v, int position) {
                historyList.remove(position);
                historyRecyclerView.removeViewAt(position);
                historyAdapter.notifyItemRemoved(position);
                historyAdapter.notifyItemRangeChanged(position, historyList.size());
                historyAdapter.notifyDataSetChanged();
                checkIfEmpty();
            }
        });
        checkIfEmpty();
        historyRecyclerView.setHasFixedSize(true);
        historyRecyclerView.setNestedScrollingEnabled(false);
        historyRecyclerView.setLayoutManager(layoutManager2);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    //clear history list
    public void onClear(View view) {
        historyList.clear();
        myDb.dropHistoryTable();
        historyRecyclerView.setAdapter(historyAdapter);
        recentTV.setText("");
        clearTV.setText("");
        noHistoryTV.setText("Search for food products");
        noHistoryIV.setBackgroundResource(R.drawable.search);
    }

    //checks if historyList is empty to determine layout
    public void checkIfEmpty() {
        if(historyList.isEmpty()) {
            recentTV.setText("");
            clearTV.setText("");
            noHistoryTV.setText("Search for food products");
            noHistoryIV.setBackgroundResource(R.drawable.search);
        } else {
            recentTV.setText("Recent searches");
            clearTV.setText("Clear recent searches");
        }
    }

    //used for the user's input into the searchbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                historyRecyclerView.setVisibility(View.GONE);
                recentTV.setVisibility(View.GONE);
                clearTV.setVisibility(View.GONE);
                noHistoryTV.setVisibility(View.GONE);
                noHistoryTV2.setVisibility(View.GONE);
                noHistoryIV.setVisibility(View.GONE);
                searchRecyclerView.setVisibility(View.VISIBLE);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
}