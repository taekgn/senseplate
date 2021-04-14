package kent.group8.senseplateandroid;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class LineChartActivity extends BaseActivity {

    private DatabaseSQLite myDb;
    protected Typeface tfRegular;
    private LineChart chart;
    private RecyclerView optionsRV;
    private ArrayList<OptionsStore> optionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        optionsRV = (RecyclerView) findViewById(R.id.optionsRV);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        optionsRV.setLayoutManager(llm);
        optionsRV.setHasFixedSize(true);

        initializeAdapter();

        setData("Calories", this);
    }

    public void setData(String option, Context context) {

        myDb = new DatabaseSQLite(context);

        ArrayList<Entry> values = new ArrayList<>();

        Float highestValue = 0f;
        Float yMax = 0f;
        int calorieGoal = 0;

        if(option.isEmpty() || option.equals("Calories")) {
            int i = 0;
            Cursor cursor = myDb.getUniqueDatesDiary();
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    Cursor cursor2 = myDb.getDailyCaloriesFromDate(date);
                    if (cursor2.moveToFirst()) {
                        do {
                            Float calories = cursor2.getFloat(0);
                            values.add(new BarEntry(i, calories));
                            i++;
                            if(calories > highestValue) {
                                highestValue = calories;
                                yMax = Float.parseFloat(String.valueOf(Math.round(calories/1000)*1000)) + 1000;
                            }
                        } while (cursor2.moveToNext());
                    }
                } while (cursor.moveToNext());
            }

            Cursor cursor3 = myDb.getCalorieTarget();
            if (cursor3.moveToFirst()) {
                do {
                    calorieGoal = cursor3.getInt(0);
                    if(calorieGoal != 0) {
                        yMax = Float.parseFloat(String.valueOf(Math.round(calorieGoal / 1000) * 1000)) + 1000;
                    }
                } while (cursor3.moveToNext()) ;
            }
        }
        else if(option.equals("Carbs")) {
            int i = 0;
            Cursor cursor = myDb.getUniqueDatesDiary();
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    Cursor cursor2 = myDb.getCarbs(date);
                    if (cursor2.moveToFirst()) {
                        do {
                            Float carbs = cursor2.getFloat(0);
                            values.add(new BarEntry(i, carbs));
                            i++;
                            if(carbs > highestValue) {
                                highestValue = carbs;
                                yMax = Float.parseFloat(String.valueOf(Math.round(carbs/100)*100))  + 100;
                            }

                        } while (cursor2.moveToNext());
                    }
                } while (cursor.moveToNext());
            }
        }
        else if(option.equals("Protein")) {
            int i = 0;
            Cursor cursor = myDb.getUniqueDatesDiary();
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    Cursor cursor2 = myDb.getProtein(date);
                    if (cursor2.moveToFirst()) {
                        do {
                            Float protein = cursor2.getFloat(0);
                            values.add(new BarEntry(i, protein));
                            i++;
                            if(protein > highestValue) {
                                highestValue = protein;
                                yMax = Float.parseFloat(String.valueOf(Math.round(protein/100)*100)) + 100;
                            }

                        } while (cursor2.moveToNext());
                    }
                } while (cursor.moveToNext());
            }
        }
        else if(option.equals("Fats")) {
            int i = 0;
            Cursor cursor = myDb.getUniqueDatesDiary();
            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    Cursor cursor2 = myDb.getFat(date);
                    if (cursor2.moveToFirst()) {
                        do {
                            Float fat = cursor2.getFloat(0);
                            values.add(new BarEntry(i, fat));
                            i++;
                            if(fat > highestValue) {
                                highestValue = fat;
                                yMax = Float.parseFloat(String.valueOf(Math.round(fat/100)*100)) + 100;
                            }

                        } while (cursor2.moveToNext());
                    }
                } while (cursor.moveToNext());
            }
        }


        Log.i("HIGEHST VALUE ", String.valueOf(yMax));




        tfRegular = Typeface.createFromAsset(context.getAssets(), "OpenSans-Regular.ttf");


        {   // // Chart Style // //
            chart = ((LineChartActivity) context).findViewById(R.id.chart1);
            chart.removeAllViews();

            // background color
            chart.setBackgroundColor(Color.WHITE);

            // disable description text
            chart.getDescription().setEnabled(false);

            // enable touch gestures
            chart.setTouchEnabled(true);

            // set listeners
            chart.setDrawGridBackground(false);


            // enable scaling and dragging
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            chart.setPinchZoom(true);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(yMax);
            yAxis.setAxisMinimum(0f);
        }

        {   // // Create Limit Lines // //
            if(calorieGoal > 0) {
                LimitLine ll1 = new LimitLine(calorieGoal, "Calorie Goal");
                ll1.setLineWidth(4f);
                ll1.enableDashedLine(10f, 10f, 0f);
                ll1.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
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
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);

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

        // set the filled area
        set1.setDrawFilled(false);
        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return chart.getAxisLeft().getAxisMinimum();
            }
        });


        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);
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
