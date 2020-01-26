package gr.hua.www.v2of2.charts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import gr.hua.www.v2of2.R;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;


import gr.hua.www.v2of2.BaseActivity;

public class ProvidersActivity extends BaseActivity {
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_providers);
        //Check if user has logged in
        if (providers.isEmpty()) {
            Toast.makeText(this, "Please Login First", Toast.LENGTH_LONG).show();
        }

        pieChart = (PieChart) findViewById(R.id.piechart);
        //Customize Chart
        pieChart.setUsePercentValues(true);
        //    pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        //Customize Chart Description
        Description description = new Description();
        description.setText("Providers");
        description.setTextSize(20);
        pieChart.setDescription(description);

        pieChart.animateY(1000, Easing.EaseInOutCubic);

        PieDataSet dataSet = new PieDataSet(providers, "Provider");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData((dataSet));
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.YELLOW);

        pieChart.setData(data);
    }
}