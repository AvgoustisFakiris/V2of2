package gr.hua.www.v2of2.charts;

import android.content.Intent;
import android.graphics.Color;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;


import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.IOException;

import gr.hua.www.v2of2.LoginActivity;
import gr.hua.www.v2of2.R;

public class StatisticsActivity extends LoginActivity implements AdapterView.OnItemSelectedListener {

    private final String TAG = getClass().getName(); // logging purposes


    BarChart barChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        //Check if user has logged in
        if (TOKEN == null) {
            Toast.makeText(this, "Please Login First", Toast.LENGTH_LONG).show();
        }
        //Check if there is no data, for user's entries
        if (avgstat.isEmpty()) {
            Toast.makeText(this, "Data is not available", Toast.LENGTH_LONG).show();
        }

        //User's options for Measurement
        Spinner spinner1 = findViewById(R.id.sp1);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.measurement, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(this);

        //User's options for Measurement
        Spinner spinner2 = findViewById(R.id.sp2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.network, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(this);


        barChart = findViewById(R.id.barchart);
        //Parse Data
        BarDataSet barDataSet1 = new BarDataSet(avgstat, "Average");
        barDataSet1.setColor(Color.RED);
        BarDataSet barDataSet2 = new BarDataSet(minstat, "Min");
        barDataSet2.setColor(Color.BLUE);
        BarDataSet barDataSet3 = new BarDataSet(maxstat, "Max");
        barDataSet3.setColor(Color.BLACK);
        //Set data to chart
        BarData data = new BarData(barDataSet1, barDataSet2, barDataSet3);
        data.notifyDataChanged();
        barChart.setData(data);
        barChart.animateY(2000);
        // Set no data text
        barChart.setNoDataText("Loading...");

        //Refresh chart if there are changes
        data.notifyDataChanged();
        barChart.refreshDrawableState();
        barChart.notifyDataSetChanged();
        barChart.invalidate();

        //Customize Chart Description
        barChart.getDescription().setEnabled(false);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(prov));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(3);
        //Customize bars
        float barSpace = 0.08f;
        float groupSpace = 0.44f;
        data.setBarWidth(0.10f);

        //     barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * 4);
        //   barChart.getAxisLeft().setAxisMaximum(0);

        barChart.groupBars(0, groupSpace, barSpace);


        //     barChart.setLogEnabled(true);

    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        Spinner spin = (Spinner) parent;
        Spinner spin2 = (Spinner) parent;
        if (spin.getId() == R.id.sp1) {
            if (position == 0) {
                measu = "levelStats/";
            } else if (position == 1) {
                measu = "uplinkStats/";
            } else {
                measu = "downlinkStats/";
            }
        }
        if (spin2.getId() == R.id.sp2) {
            if (position == 0) {
                netw = "";
            } else if (position == 1) {
                netw = "2G";
            } else if (position == 2) {
                netw = "3G";
            } else {
                netw = "4G";
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    public void btnSubmit(View view) throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());

        //Clear Chart
        barChart.clear();

        //Run statisticsOperator
        statisticsOperator();

        //Refresh Activity (Mpandroid Bug)
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = getIntent();
                finish();
                startActivity(intent);

            }
        }, 3000);


    }

}
