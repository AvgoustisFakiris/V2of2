package gr.hua.www.v2of2.charts;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import gr.hua.www.v2of2.BaseActivity;

import gr.hua.www.v2of2.R;

public class StatisticsActivity extends BaseActivity {

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        //Check if user has logged in
        if (avgstat.isEmpty()) {
            Toast.makeText(this, "Please Login First", Toast.LENGTH_LONG).show();
        }

        barChart = (BarChart) findViewById(R.id.barchart);
        //Parse Data
        BarDataSet barDataSet1 = new BarDataSet(avgstat, "Average");
        barDataSet1.setColor(Color.RED);
        BarDataSet barDataSet2 = new BarDataSet(minstat, "Min");
        barDataSet2.setColor(Color.BLUE);
        BarDataSet barDataSet3 = new BarDataSet(maxstat, "Max");
        barDataSet3.setColor(Color.MAGENTA);
        //Set data to chart
        BarData data = new BarData(barDataSet1, barDataSet2, barDataSet3);
        barChart.setData(data);

        //Customize Chart Description
        barChart.getDescription().setEnabled(false);

        String[] providers = new String[]{"Cosmote", "Other", "Vodafone", "Wind"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(providers));
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

        //   barChart.getXAxis().setAxisMinimum(0);
        //   barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * 4);
        //   barChart.getAxisLeft().setAxisMaximum(0);

        barChart.groupBars(0, groupSpace, barSpace);
        //Refresh chart if there are changes
        // barChart.notifyDataSetChanged();

        barChart.invalidate();
    }
}

