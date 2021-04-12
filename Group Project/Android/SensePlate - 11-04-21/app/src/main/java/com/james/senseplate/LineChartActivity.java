package com.james.senseplate;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;

public class LineChartActivity extends BaseActivity {

    private DatabaseSQLite myDb;
    protected Typeface tfRegular;
    private LineChart chart;
    private RecyclerView optionsRV;
    private ArrayList<OptionsStore> optionsList;
    private ArrayList<String> xValuesList;

    private TextView tvDate;
    private ImageButton buttonPrev, buttonNext;
    private int dateCounter = 0;
    LocalDate startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        optionsRV = (RecyclerView) findViewById(R.id.optionsRV);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        optionsRV.setLayoutManager(llm);
        optionsRV.setHasFixedSize(true);


        buttonPrev = (ImageButton) findViewById(R.id.buttonPrevious);
        buttonNext = (ImageButton) findViewById(R.id.buttonNext);

        //button to go to previous day
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dateCounter -= 7;
                setData("Calories", LineChartActivity.this);
            }
        });
        //button to go to next day
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dateCounter += 7;
                setData("Calories", LineChartActivity.this);
            }
        });


        initializeAdapter();

        setData("Calories", this);
    }



    public void setData(String option, Context context) {

        myDb = new DatabaseSQLite(context);
        xValuesList = new ArrayList<>();

        ArrayList<Entry> values = new ArrayList<>();

        Float highestValue = 0f;
        Float yMax = 0f;
        int calorieGoal = 0;

        LocalDate date2 = LocalDate.now().plusDays(dateCounter);
        startDate = date2;
        while (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            startDate = startDate.minusDays(1);
        }
        endDate = date2;
        while (endDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            endDate = endDate.plusDays(1);
        }

        String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("MMM dd"));
        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("MMM dd yyyy"));

        //needs to be initialised here to as it's called from a different activity and requires context for it to be called.
        tvDate = (TextView) ((Activity)context).findViewById(R.id.tvDate);
        tvDate.setText(formattedStartDate + " - " + formattedEndDate);


        String formattedStartDateDB = startDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String formattedEndDateDB = endDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        int counter = 0;
        Cursor cursor = myDb.getUniqueDatesDiary(formattedStartDateDB, formattedEndDateDB);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);

                //convert the date from a string to a date to allow it to be parsed as a day instead e.g. 2021/03/29 becomes Mon
                DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate ld = LocalDate.parse(date, DATEFORMATTER);
                LocalDateTime ldt = LocalDateTime.of(ld, LocalDateTime.now().toLocalTime());

                String day = ldt.format(DateTimeFormatter.ofPattern("E"));
                xValuesList.add(String.valueOf(day));

                if(option.isEmpty() || option.equals("Calories")) {

                    Cursor cursorCalories = myDb.getDailyCaloriesFromDate(date);
                    if (cursorCalories.moveToFirst()) {
                        do {
                            Float calories = cursorCalories.getFloat(0);
                            values.add(new BarEntry(counter, calories));
                            counter++;
                            //check if the daily calories is higher than previous highest values
                            if (calories > highestValue) {
                                highestValue = calories;
                                yMax = Float.parseFloat(String.valueOf(Math.round(highestValue / 1000) * 1000)) + 1000;
                            }
                        } while (cursorCalories.moveToNext());

                        Cursor cursorCalorieTarget = myDb.getCalorieTarget();
                        if (cursorCalorieTarget.moveToFirst()) {
                            do {
                                calorieGoal = cursorCalorieTarget.getInt(0);
                            } while (cursorCalorieTarget.moveToNext());
                        }
                    }
                }
                else if (option.equals("Carbs")) {
                    Cursor cursorCarbs = myDb.getCarbs(date);
                    if (cursorCarbs.moveToFirst()) {
                        do {
                            Float carbs = cursorCarbs.getFloat(0);
                            values.add(new BarEntry(counter, carbs));
                            counter++;
                            if (carbs > highestValue) {
                                highestValue = carbs;
                                yMax = Float.parseFloat(String.valueOf(Math.round(highestValue / 100) * 100)) + 100;
                            }
                        } while (cursorCarbs.moveToNext());
                    }
                }
                else if (option.equals("Protein")) {
                    Cursor cursorProtein = myDb.getProtein(date);
                    if (cursorProtein.moveToFirst()) {
                        do {
                            Float protein = cursorProtein.getFloat(0);
                            Log.i("PROTEIN", ""+protein);
                            Log.i("Counter", ""+ counter);
                            values.add(new BarEntry(counter, protein));
                            counter++;
                            if (protein > highestValue) {
                                highestValue = protein;
                                yMax = Float.parseFloat(String.valueOf(Math.round(highestValue / 100) * 100)) + 100;
                            }
                        } while (cursorProtein.moveToNext());
                    }
                }
                else if (option.equals("Fats")) {
                    Cursor cursorFats = myDb.getFat(date);
                    if (cursorFats.moveToFirst()) {
                        do {
                            Float fat = cursorFats.getFloat(0);
                            values.add(new BarEntry(counter, fat));
                            counter++;
                            if (fat > highestValue) {
                                highestValue = fat;
                                yMax = Float.parseFloat(String.valueOf(Math.round(highestValue / 100) * 100)) + 100;
                            }

                        } while (cursorFats.moveToNext());
                    }
                }
            } while (cursor.moveToNext());
        }

        tfRegular = Typeface.createFromAsset(context.getAssets(), "OpenSans-Regular.ttf");

        {   // // Chart Style // //
            chart = ((LineChartActivity) context).findViewById(R.id.chart1);
            chart.removeAllViews();

            // background color
            chart.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

            // disable description text
            chart.getDescription().setEnabled(false);

            // enable touch gestures
            chart.setTouchEnabled(false);

            // set listeners
            chart.setDrawGridBackground(false);


            // enable scaling and dragging
            chart.setDragEnabled(false);
            chart.setScaleEnabled(true);

            // force pinch zoom along both axis
            chart.setPinchZoom(false);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            xAxis.setGranularity(1f); // only intervals of 1 day
            xAxis.setLabelRotationAngle(0); //rotate x-axis labels
            xAxis.setTextColor(Color.WHITE);

            ValueFormatter xAxisFormatter = new xAxisValueFormatter(chart);
            xAxis.setValueFormatter(xAxisFormatter);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setTextColor(Color.WHITE);
            yAxis.setAxisMaximum(yMax);
            yAxis.setAxisMinimum(0f);
        }

        {   // // Create Limit Lines // //
            if(calorieGoal > 0) {
                LimitLine ll1 = new LimitLine(calorieGoal, "Calorie Goal");
                ll1.setLineWidth(4f);
                ll1.enableDashedLine(10f, 10f, 0f);
                ll1.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
                ll1.setLineColor(Color.WHITE);
                ll1.setTextColor(Color.WHITE);
                ll1.setTextSize(10f);
                ll1.setTypeface(tfRegular);
                // draw limit lines behind data instead of on top
                yAxis.setDrawLimitLinesBehindData(true);

                // add limit lines
                yAxis.addLimitLine(ll1);
            }

        }

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // draw legend entries as lines
        l.setForm(LegendForm.LINE);

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(values, option);
        set1.setDrawIcons(false);

        // draw dashed line
        set1.enableDashedLine(10f, 5f, 0f);

        // black lines and points
        set1.setColor(Color.WHITE);
        set1.setValueTextColor(Color.WHITE);
        set1.setCircleColor(Color.WHITE);
        set1.setHighLightColor(Color.WHITE);

        // line thickness and point size
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);

        // draw points as solid circles
        set1.setDrawCircleHole(false);

        // customize legend entry
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        // text size of values
        set1.setValueTextSize(9f);

        // draw selection line as dashed
        set1.enableDashedHighlightLine(10f, 5f, 0f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);
    }

    //changes the x-axis to allow the date to be shown
    public class xAxisValueFormatter extends ValueFormatter {
        private final LineChart chart;
        public xAxisValueFormatter(LineChart chart) {
            this.chart = chart;
        }
        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            String val = "STEWPID";
            try {
                val = xValuesList.get(index);
            }
            catch (ArrayIndexOutOfBoundsException e) {

            }
            catch (IndexOutOfBoundsException e) {

            }

            return val;
        }
    }

    private void initializeAdapter() {
        optionsList = new ArrayList<>();

        optionsList.add(new OptionsStore("Calories", "icon_calories"));
        optionsList.add(new OptionsStore("Carbs", "icon_carbs"));
        optionsList.add(new OptionsStore("Protein", "icon_protein"));
        optionsList.add(new OptionsStore("Fats", "icon_fat"));


        OptionsAdapter adapter = new OptionsAdapter(optionsList, this);
        optionsRV.setAdapter(adapter);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_linechart;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_compare;
    }
}

