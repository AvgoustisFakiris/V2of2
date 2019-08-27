package gr.hua.www.v2of2.charts;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

import gr.hua.www.v2of2.R;

public class StatisticsActivity extends AppCompatActivity {

    BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        barChart = (BarChart) findViewById(R.id.barchart);

        BarDataSet barDataSet1 = new BarDataSet(barEntries1(), "Average");
        barDataSet1.setColor(Color.RED);
        BarDataSet barDataSet2 = new BarDataSet(barEntries2(), "Min");
        barDataSet2.setColor(Color.BLUE);
        BarDataSet barDataSet3 = new BarDataSet(barEntries3(), "Max");
        barDataSet3.setColor(Color.MAGENTA);

        BarData data = new BarData(barDataSet1, barDataSet2, barDataSet3);
        barChart.setData(data);

        String[] providers = new String[]{"Vodafone", "Cosmote", "Wind", "Other"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(providers));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);

        barChart.setDragEnabled(true);
        barChart.setVisibleXRangeMaximum(3);

        float barSpace = 0.08f;
        float groupSpace = 0.44f;
        data.setBarWidth(0.10f);

        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * 4);
        barChart.getAxisLeft().setAxisMaximum(0);

        barChart.groupBars(0, groupSpace, barSpace);

        barChart.invalidate();
    }

    private ArrayList<BarEntry> barEntries1() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 2000));
        barEntries.add(new BarEntry(2, 761));
        barEntries.add(new BarEntry(3, 230));
        barEntries.add(new BarEntry(4, 600));

        return barEntries;

    }


    private ArrayList<BarEntry> barEntries2() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 500));
        barEntries.add(new BarEntry(2, 1761));
        barEntries.add(new BarEntry(3, 280));
        barEntries.add(new BarEntry(4, 650));

        return barEntries;

    }


    private ArrayList<BarEntry> barEntries3() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, 2100));
        barEntries.add(new BarEntry(2, 751));
        barEntries.add(new BarEntry(3, 630));
        barEntries.add(new BarEntry(4, 800));

        return barEntries;

    }

}

