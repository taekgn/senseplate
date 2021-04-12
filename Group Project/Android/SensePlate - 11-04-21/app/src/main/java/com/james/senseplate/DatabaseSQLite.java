package com.james.senseplate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseSQLite extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "senseplate.db";
    public static final String TABLE_HISTORY = "search_history";
    public static final String TABLE_DIARY = "food_diary";
    public static final String TABLE_PROFILE = "profile";
    public static final String TABLE_DEVICES = "devices";

    public static final String HIS_COL_1 = "id";
    public static final String HIS_COL_2 = "food";

    public static final String DIA_COL_1 = "id";
    public static final String DIA_COL_2 = "foodID";
    public static final String DIA_COL_3 = "foodItem";
    public static final String DIA_COL_4 = "calories";
    public static final String DIA_COL_5 = "carbs";
    public static final String DIA_COL_6 = "protein";
    public static final String DIA_COL_7 = "fat";
    public static final String DIA_COL_8 = "temperature";
    public static final String DIA_COL_9 = "moisture";
    public static final String DIA_COL_10 = "mealType";
    public static final String DIA_COL_11 = "date";
    public static final String DIA_COL_12 = "time";

    public static final String PROF_COL_1 = "id";
    public static final String PROF_COL_2 = "gender";
    public static final String PROF_COL_3 = "age";
    public static final String PROF_COL_4 = "height";
    public static final String PROF_COL_5 = "weight";
    public static final String PROF_COL_6 = "bmi";
    public static final String PROF_COL_7 = "activity";
    public static final String PROF_COL_8 = "goal";
    public static final String PROF_COL_9 = "calorieGoal";

    public static final String DEV_COL_1 = "id";
    public static final String DEV_COL_2 = "deviceName";

    public DatabaseSQLite(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_HISTORY + " (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, food TEXT)");
        db.execSQL("create table " + TABLE_DIARY + " (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, foodID TEXT, foodItem TEXT, calories TEXT, carbs TEXT, protein TEXT, fat TEXT, temperature TEXT, moisture TEXT, mealType TEXT, date TEXT, time TEXT)");
        db.execSQL("create table " + TABLE_PROFILE + " (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, gender TEXT, age INTEGER, height REAL, weight REAL, bmi REAL, activity TEXT, goal TEXT, calorieGoal REAL)");
        db.execSQL("create table " + TABLE_DEVICES + " (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, deviceName TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        onCreate(db);
    }

    public void dropHistoryTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
    }

    public boolean insertHistory(String food) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(HIS_COL_2, food);
        long result = db.insert(TABLE_HISTORY,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT id, food FROM search_history ORDER BY id DESC;",null);
        return data;
    }

    public boolean removeItemHistory(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_HISTORY,"id = ?", new String[] { id });
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor findMultipleHistory(String food) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT id, count(id) FROM search_history WHERE food = '" + food + "'",null);
        return data;
    }

    public boolean removeMultipleHistory(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_HISTORY,"id = ?", new String[] {id});
        if(result == -1)
            return false;
        else
            return true;
    }

    public boolean insertFoodDiary(String foodID, String foodItem, String calories, Float carbs, Float protein, Float fat, Float temperature, Float moisture, String mealType, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DIA_COL_2, foodID);
        contentValues.put(DIA_COL_3, foodItem);
        contentValues.put(DIA_COL_4, calories);
        contentValues.put(DIA_COL_5, carbs);
        contentValues.put(DIA_COL_6, protein);
        contentValues.put(DIA_COL_7, fat);
        contentValues.put(DIA_COL_8, temperature);
        contentValues.put(DIA_COL_9, moisture);
        contentValues.put(DIA_COL_10, mealType);
        contentValues.put(DIA_COL_11, date);
        contentValues.put(DIA_COL_12, time);
        long result = db.insert(TABLE_DIARY,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getFoodDiary(String whereQuery) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT id, foodID, foodItem, calories, carbs, protein, fat, temperature, moisture, mealType, date, time FROM food_diary " + whereQuery + " ORDER BY id ASC;",null);
        return data;
    }

    public Cursor getFoodDiaryGraph() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT id, foodItem, calories, carbs, protein, fat, temperature, moisture, date, time FROM food_diary ORDER BY date ASC;",null);
        return data;
    }

    public Cursor getUniqueDatesDiary(String startDate, String endDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery( "SELECT DISTINCT date FROM food_diary WHERE date BETWEEN '" + startDate + "' AND '" + endDate + "' ORDER BY date ASC;",null);
        return data;
    }

    public Cursor getDailyCaloriesFromDate(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery( "SELECT sum(calories) FROM food_diary WHERE date = '" + date + "';",null);
        return data;
    }

    public Cursor getCarbs(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery( "SELECT sum(carbs) FROM food_diary WHERE date = '" + date + "';",null);
        return data;
    }

    public Cursor getProtein(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery( "SELECT sum(protein) FROM food_diary WHERE date = '" + date + "';",null);
        return data;
    }

    public Cursor getFat(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery( "SELECT sum(fat) FROM food_diary WHERE date = '" + date + "';",null);
        return data;
    }


    public boolean removeFoodDiary(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_DIARY,"id = ?", new String[] { id });
        if(result == -1)
            return false;
        else
            return true;
    }

    public void dropProfileTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROFILE, null, null);
    }

    public boolean insertProfile(String gender, int age, Double height, Double weight, Double bmi, String activity, String goal, Double calorieGoal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PROF_COL_2, gender);
        contentValues.put(PROF_COL_3, age);
        contentValues.put(PROF_COL_4, height);
        contentValues.put(PROF_COL_5, weight);
        contentValues.put(PROF_COL_6, bmi);
        contentValues.put(PROF_COL_7, activity);
        contentValues.put(PROF_COL_8, goal);
        contentValues.put(PROF_COL_9, calorieGoal);
        long result = db.insert(TABLE_PROFILE,null ,contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    public void insertProfileDefault() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PROF_COL_1, 0);
        contentValues.put(PROF_COL_2, "");
        contentValues.put(PROF_COL_3, "");
        contentValues.put(PROF_COL_4, "");
        contentValues.put(PROF_COL_5, "");
        contentValues.put(PROF_COL_6, "");
        contentValues.put(PROF_COL_7, "");
        contentValues.put(PROF_COL_8, "");
        contentValues.put(PROF_COL_9, "");
        db.insert(TABLE_PROFILE,null ,contentValues);
    }

    public void updateProfileSingle(String name,  String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if(name.equals("Gender")) {
            contentValues.put(PROF_COL_2, value);
        }
        else if(name.equals("Age")) {
            contentValues.put(PROF_COL_3, Integer.valueOf(value));
        }
        else if(name.equals("Height")) {
            contentValues.put(PROF_COL_4, Double.valueOf(value));
        }
        else if(name.equals("Weight")) {
            contentValues.put(PROF_COL_5, Double.valueOf(value));
        }
        else if(name.equals("BMI")) {
            contentValues.put(PROF_COL_6, Double.valueOf(value));
        }
        else if(name.equals("Activity")) {
            contentValues.put(PROF_COL_7, value);
        }
        else if(name.equals("Goal")) {
            contentValues.put(PROF_COL_8, value);
        }
        else if(name.equals("Calorie Target")) {
            contentValues.put(PROF_COL_9, Double.valueOf(value));
        }
        db.update(TABLE_PROFILE, contentValues, "id = 0", null); //id doesn't change so set to 0
    }

    public Cursor getProfile() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT id, gender, age, height, weight, bmi, activity, goal, calorieGoal FROM profile ORDER BY id DESC;",null);
        return data;
    }

    public Cursor getCalorieTarget() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT calorieGoal FROM profile WHERE id = 0;",null);
        return data;
    }

    public boolean insertDevice(String deviceName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DEV_COL_1, 0);
        contentValues.put(DEV_COL_2, deviceName);
        long result = db.insert(TABLE_DEVICES,null ,contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    public void updateDevice(String deviceName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DEV_COL_2, deviceName);
        db.update(TABLE_DEVICES, contentValues, "id = 0", null); //id doesn't change so set to 0
    }

    public Cursor getDevice() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT COUNT(1), deviceName FROM devices WHERE id = 0;",null);
        return data;
    }

    public Cursor removeDevice() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("DELETE FROM devices WHERE id = 0;",null);
        return data;
    }
}
