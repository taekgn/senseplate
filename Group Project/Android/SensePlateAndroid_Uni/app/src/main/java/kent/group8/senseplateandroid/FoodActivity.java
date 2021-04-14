package kent.group8.senseplateandroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

//////////////////////////////////
import android.os.PowerManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.io.IOException;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Message;
import android.app.NotificationManager;
import android.util.Log;
import android.widget.Toast;

///////////////////////////////////
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
//////////////////////////////////

public class FoodActivity extends AppCompatActivity {
    private String foodID, foodItem, amount, calories, protein, fat, carbs;
    private float weight, carbsF, fatF, proteinF, energy;
    private TextView tvFood, tvCalories, tvGoals, tvCarbs, tvFat, tvProtein;
    private PieChart pieChart;
 //   private ProgressBar progressBar;
    private CardView cardView;
    private Button addButton;
    private DatabaseSQLite myDB;
    private String rxdatum;

    float mass, mio, cole, organ, energyF;
    int energyI;
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
    private TextView tvMoi;
    private TextView tvTemp;
    private Button refresh;
    //////////////////////////////////////////////

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodactivity_main);

        myDB = new DatabaseSQLite(this);
        tvFood = (TextView) findViewById(R.id.textViewFood);
        tvCalories = (TextView) findViewById(R.id.textViewCalories);
        tvGoals = (TextView) findViewById(R.id.textViewGoal);
        cardView = (CardView) findViewById(R.id.cardView);
        cardView.setVisibility(View.GONE);
    //    progressBar = (ProgressBar) findViewById(R.id.progressBar);
        pieChart = (PieChart) findViewById(R.id.piechart);
        tvCarbs = (TextView) findViewById(R.id.tvCarbs);
        tvFat = (TextView) findViewById(R.id.tvFat);
        tvProtein = (TextView) findViewById(R.id.tvProtein);
        addButton = (Button) findViewById(R.id.addButton);

        //retrieve strings from SearchAdapter
        Intent intent = getIntent();
        foodID = intent.getExtras().getString("foodID");
        foodItem = intent.getExtras().getString("foodItem");
        amount = intent.getExtras().getString("amount");
        calories = intent.getExtras().getString("calories");
        protein = intent.getExtras().getString("protein");
        fat = intent.getExtras().getString("fat");
        carbs = intent.getExtras().getString("carbs");

        tvTemp = findViewById(R.id.tvTemp);
        tvMoi = findViewById(R.id.tvMoi);
        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signal.addWeight(rxdatum.substring(0,4));
                createTextView();
                createPieChart(signal.getWeight());
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    rxdatum = null;
                    try {
                        rxdatum = new String((byte[]) msg.obj, "UTF-8");
                        signal.addWeight(rxdatum.substring(0,4));
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



        //set textviews for name, calories and goal
        tvFood.setText(foodItem);
        //tvCalories.setText(calories.substring(0, calories.indexOf(".")) + " cal");
        //will be used to show daily goal once set up

        Cursor cursor = myDB.getCalorieTarget();
        if (cursor.moveToFirst()) {
            do {
                Double caloriesTarget = cursor.getDouble(0);
                Double caloriesPerc = (Double.valueOf(calories) / caloriesTarget) * 100;
                tvGoals.setText(String.format("%.2f", caloriesPerc) + "% of daily goal");
            } while (cursor.moveToNext());
        }


        //button to add the food item to the user's diary
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StoreInfo store = new StoreInfo();
                String mealType = store.getMealType();
                String date = store.getDate();
                String time = store.getTime();
                myDB.insertFoodDiary(foodID, foodItem, calories, carbsF, proteinF, fatF, mealType, date, time);
                Intent i = new Intent(getApplicationContext(), DiaryActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        //will be used to show loading of weighing food
  //      progressBar.setProgress(75);

    }

    public void makeplot(){
        if(state == true && second == true){
            second = false;//second switch is disabled to prevent repeatition and collision
            createPieChart(signal.getWeight());
            createTextView();
        }
        else{

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
                        signal.addWeight("100");
                        createPieChart(signal.getWeight());
                        createTextView();

                    }
                })
                .show();
    }

    public void createTextView(){
        TextView tvWeight = findViewById(R.id.textViewWeight);
        tvWeight.setText(signal.getWeight());


        mass = Float.parseFloat(signal.getWeight()) / 100;
        mio = Float.parseFloat(protein) * mass;//protein
        cole = Float.parseFloat(fat) * mass;//fat
        organ = Float.parseFloat(carbs) * mass;//carbs
        energyF = Float.parseFloat(calories) * mass;
        energyI = Math.round(energyF);//calorie

        tvCalories.setText(energyI + " cal");
        tvCarbs.setText("Carbs " + organ + "g");
        tvFat.setText("Fat " + cole + "g");
        tvProtein.setText("Protein " + mio + "g");
        tvMoi.setText(signal.getMoi());
        tvTemp.setText(signal.getTemp());

        tvTemp.setVisibility(View.VISIBLE);
        tvMoi.setVisibility(View.VISIBLE);
    }

    //to create pie chart showing nutrition percentages
    public void createPieChart(String sign)
    {
        cardView.setVisibility(View.VISIBLE);

        weight = Float.parseFloat(sign);
        weight /= 100;
        carbsF = Float.parseFloat(carbs);
        fatF = Float.parseFloat(fat);
        proteinF = Float.parseFloat(protein);
        energy = Float.parseFloat(calories);

        carbsF *= weight;
        fatF *= weight;
        proteinF *= weight;
        energy *= weight;


        //set key
        //tvCarbs.setText("Carbs " + carbs + "g");
        //tvFat.setText("Fat " + fat + "g");
        //tvProtein.setText("Protein " + protein + "g");
   //     pieChart.setInnerPaddingColor(Color.parseColor("#121212"));
        pieChart.clearChart();
        // Set the data and color to the pie chart
        pieChart.addPieSlice(
                new PieModel(
                        carbsF,
                        Color.parseColor("#29B6F6")));
        pieChart.addPieSlice(
                new PieModel(
                        fatF,
                        Color.parseColor("#EF5350")));
        pieChart.addPieSlice(
                new PieModel(
                        proteinF,
                        Color.parseColor("#66BB6A")));

        // To animate the pie chart
        pieChart.startAnimation();
    }


    public void bluetoothOn() {
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't support bluetooth.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Already actiavted.", Toast.LENGTH_LONG).show();
                //mTvBluetoothStatus.setText("Activate");
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
            //mTvBluetoothStatus.setText("Deactivate");
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
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
        return super.onOptionsItemSelected(item);
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
                        try{
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();}
                        catch (NullPointerException e){}
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
                Toast.makeText(getApplicationContext(), "Erro while ejecting socket.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
