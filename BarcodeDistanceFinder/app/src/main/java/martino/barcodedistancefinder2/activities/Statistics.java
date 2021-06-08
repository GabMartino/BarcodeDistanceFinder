package martino.barcodedistancefinder2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

import martino.barcodedistancefinder2.R;

public class Statistics extends AppCompatActivity implements View.OnClickListener {
    LineChart chart;

    double securityPercentage = 0.2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getSupportActionBar().hide();
        setTitle("Statistics");
        setContentView(R.layout.activity_statistics);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String v = prefs.getString(getString(R.string.DangerousLevelKey),"");
        securityPercentage =new Float(Float.valueOf(v));
        System.out.println(securityPercentage);
        chart = (LineChart) findViewById(R.id.chart);
        Button clearDataButton = (Button) findViewById(R.id.clearDataButton);
        clearDataButton.setOnClickListener(this);


        // no description text
        chart.getDescription().setEnabled(false);
        YAxis y = chart.getAxis(
                YAxis.AxisDependency.LEFT
        );
        y.setAxisMinimum(0f);

        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                //
        // enable / disable grid background
//        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);

        // enable touch gestures
        chart.setTouchEnabled(true);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setBackgroundColor(getColor(R.color.light_gray));


        // set custom chart offsets (automatic offset calculation is hereby disabled)
        //chart.setViewPortOffsets(-10, 0, -10, 0);
        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.animateXY(2000, 2000);
        chart.setDrawGridBackground(false);
        updateData();

    }
    private LineData  getDataSet() {

        int counter = 0;
        int nValues = 0;
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        try {
            JSONArray values = new JSONArray(prefs.getString(getString(R.string.listOfDistances), "[]"));
            System.out.println(values.toString());
            if(values != null && values.length() > 0){

                ArrayList<Entry> data = new ArrayList<>();
                for(int i = 0; i < values.length(); i++){

                    data.add(new Entry(i, (float) values.getDouble(i)/10));
                    if(values.getDouble(i)/10 < getThreshold()){
                        counter++;
                    }
                }
                nValues = values.length();
                LineDataSet set = new LineDataSet(data, "Distance");
                set.setFillFormatter(new DefaultFillFormatter(){
                    @Override
                    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                        return getThreshold();
                    }
                });
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue);
                set.setFillDrawable(drawable);
                set.setDrawFilled(true);

                set.setLineWidth(1.75f);
                //set.setColor(Color.WHITE);
                set.setCircleColor(getColor(R.color.dot_dark_screen2));
                set.setHighLightColor(getColor(R.color.dot_dark_screen2));
                //set.setCubicIntensity(0.5f);
                set.setDrawValues(false);
                float danger_level = ((float)counter/ (float)nValues);

                if( danger_level>= securityPercentage){
                    ((TextView) findViewById(R.id.behav_label)).setText("Dangerous");
                }else{
                    ((TextView) findViewById(R.id.behav_label)).setText("Good");
                }
                return new LineData(set);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;

    }
    private void updateData(){

        LineData values = getDataSet();
        if(values != null){

            chart.getXAxis().resetAxisMaximum();
            Float threshold = getThreshold();
            System.out.println(threshold);
            ArrayList<Entry> data = new ArrayList<>();
            data.add(new Entry(0, threshold));
            data.add(new Entry((values.getDataSets().get(0)).getXMax(), threshold));
            LineDataSet t = new LineDataSet(data, "Threshold");
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
            t.setDrawFilled(true);
            t.setFillDrawable(drawable);
            t.setLineWidth(1.75f);
            t.setColor(getColor(R.color.warning));
            t.setCircleColor(getColor(R.color.warning));
            t.setHighLightColor(getColor(R.color.warning));
            t.setCubicIntensity(0.2f);
            t.setDrawValues(false);
            t.setFillColor(getColor(R.color.warning));
            t.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            values.addDataSet(t);

            chart.setData(values);
        }else{
            System.out.println("no data");
            chart.getXAxis().setAxisMinimum(0);
            chart.getXAxis().setAxisMaximum(100);
            chart.clear();
        }

        chart.invalidate();
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.clearDataButton){
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(getString(R.string.listOfDistances));
            editor.commit();
            updateData();
        }
    }
    private Float getThreshold(){
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String v = prefs.getString(getString(R.string.WarningDistanceNameKey),"");
        Float threshold = new Float(Float.valueOf(v));
        return threshold;
    }
}