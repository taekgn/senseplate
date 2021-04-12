package com.james.senseplate;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FoodActivity extends AppCompatActivity {
    private String foodID, foodItem;
    private TextView tvWeight, tvFood, tvCalories, tvGoals, tvTemp, tvMoisture;
    private float weight, calories, protein, fat, carbs, temp, moisture;

    private Button addButton;
    private DatabaseSQLite myDB;

    private com.github.mikephil.charting.charts.PieChart chart;
    private Typeface tfRegular;
    private Typeface tfLight;

    private String rxdatum;

    Signalton signal = Signalton.getInstance();

    ///////////////////////////////////////////////
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean state = false;
    private boolean second = true;
    //////////////////////////////////////////////

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodactivity_main);

        myDB = new DatabaseSQLite(this);

        tvWeight = (TextView) findViewById(R.id.textViewWeight);
        tvFood = (TextView) findViewById(R.id.textViewFood);
        tvCalories = (TextView) findViewById(R.id.textViewCalories);
        tvGoals = (TextView) findViewById(R.id.textViewGoal);
        tvTemp = (TextView) findViewById(R.id.textViewTemp);
        tvMoisture = (TextView) findViewById(R.id.textViewMoist);
        addButton = (Button) findViewById(R.id.addButton);

        //button to add the food item to the user's diary
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StoreInfo store = new StoreInfo();
                String mealType = store.getMealType();
                String date = store.getDate();
                String time = store.getTime();
                myDB.insertFoodDiary(foodID, foodItem, String.format("%.0f", calories), carbs, protein, fat, temp, moisture, mealType, date, time);
                Intent i = new Intent(getApplicationContext(), DiaryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    rxdatum = null;
                    try {
                        rxdatum = new String((byte[]) msg.obj, "UTF-8");
                        if(Float.valueOf(rxdatum.replaceAll("\n.*", "")) < 3) {
                            signal.addWeight("0");
                        } else {
                            signal.addWeight(rxdatum.replaceAll("\n.*", ""));
                        }
                        state = true;//first switch on, now both switches are enabled
                        makeplot();//This is special function for BLT communication to prevent collision
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        popup();
        //Shows alertdialog regards to blt

    }

    public void makeplot(){
        if(state == true && second == true){
            second = false;//second switch is disabled to prevent repeatition and collision
            setData(signal.getWeight());
        }
    }

    public void popup(){
        AlertDialog builder = new AlertDialog.Builder(FoodActivity.this)
                .setMessage("Default mode or Bluetooth mode?")
                .setPositiveButton("Pair", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        Toast.makeText(getApplicationContext(), "PAIRING Selected", Toast.LENGTH_SHORT).show();
                        listPairedDevices();
                        //createTextView();
                        //createPieChart();

                    }
                })
                .setNegativeButton("Default", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        Toast.makeText(getApplicationContext(), "DEFAULT Selected", Toast.LENGTH_SHORT).show();
                        signal.addWeight("200");
                        setData(signal.getWeight());
                    }
                })
                .show();
    }


    public void bluetoothOn() {
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Already actiavted.", Toast.LENGTH_LONG).show();
                //mTvBluetoothStatus.setText("활성화");
            }
            else {
                Toast.makeText(getApplicationContext(), "Bluetooth is not activated.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }
    public void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "Bluetooth deactivated.", Toast.LENGTH_SHORT).show();
            //mTvBluetoothStatus.setText("비활성화");
        }
        else {
            Toast.makeText(getApplicationContext(), "Already deactivated.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // when activate presssed
                    Toast.makeText(getApplicationContext(), "Activate", Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) { // when cancel pressed
                    Toast.makeText(getApplicationContext(), "Canecel", Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Device");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "No paired devices.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Bluetooth is deactivated.", Toast.LENGTH_SHORT).show();
        }
    }

    public void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error while connecting Bluetooth.", Toast.LENGTH_LONG).show();
        }
    }

    public class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error while connecting socket.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        try {
                            bytes = mmInStream.available();
                            bytes = mmInStream.read(buffer, 0, bytes);
                            mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();}
                        catch (NullPointerException e) {

                        } catch (ArrayIndexOutOfBoundsException e) {

                        }
                    }
                } catch (IOException ne) {
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error while ejecting socket.", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void setData(String realWeight) {

        weight = Float.valueOf(realWeight);
        tvWeight.setText(String.format("%.0f", weight));

        temp = 160f;
        tvTemp.setText("N/A °C");

        moisture = 62f;
        tvMoisture.setText("N/A %");

        //retrieve strings from SearchAdapter
        Intent intent = getIntent();
        foodID = intent.getExtras().getString("foodID");
        foodItem = intent.getExtras().getString("foodItem");
        //the api sends the data at 100g. Makes the values to 1g to then mulitply by the weight
        calories = (Float.valueOf(intent.getExtras().getString("calories"))/100) * weight;
        protein = (Float.valueOf(intent.getExtras().getString("protein"))/100) * weight;
        fat = (Float.valueOf(intent.getExtras().getString("fat"))/100) * weight;
        carbs = (Float.valueOf(intent.getExtras().getString("carbs"))/100) * weight;


        tfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        chart = findViewById(R.id.chart1);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);

        chart.setCenterTextTypeface(tfLight);
        chart.setCenterText("Macros\n%");

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(58f);

        chart.setDrawCenterText(true);

        // enable rotation of the chart by touch
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);

        chart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);



        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTypeface(tfRegular);
        chart.setEntryLabelTextSize(12f);
        chart.setDrawEntryLabels(!chart.isDrawEntryLabelsEnabled());
        //set legend colour white
        chart.getLegend().setTextColor(Color.WHITE);

        tvFood.setText(foodItem);
        tvCalories.setText(String.format("%.0f", calories) + " cal");

        Cursor cursor = myDB.getCalorieTarget();
        if (cursor.moveToFirst()) {
            do {
                Double caloriesTarget = cursor.getDouble(0);
                Double caloriesPer = (Double.valueOf(calories) / caloriesTarget) * 100;
                tvGoals.setText(String.format("%.2f", caloriesPer) + "% of daily goal");
            } while (cursor.moveToNext());
        }

        ArrayList<PieEntry> entries = new ArrayList<>();

        //adding values to pie chart
        entries.add(new PieEntry(carbs, "Carbs " + String.format("%.2f", carbs) + "g"));
        entries.add(new PieEntry(fat, "Fat " + String.format("%.2f", fat) + "g"));
        entries.add(new PieEntry(protein, "Protein " + String.format("%.2f", protein) + "g"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        //adding colours, has to be same order as values
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.carbs));
        colors.add(getResources().getColor(R.color.fat));
        colors.add(getResources().getColor(R.color.protein));

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(tfLight);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_foodactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //opens search activity when search button pressed
        if (id == R.id.menu_search) {
            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        }
        if (id == R.id.menu_refresh) {
            signal.addWeight(rxdatum.replaceAll("\n.*", ""));
            setData(signal.getWeight());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
