package gr.hua.www.v2of2;

import android.content.Intent;
import android.os.Bundle;

import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import java.util.ArrayList;

import gr.hua.www.v2of2.charts.NetworksActivity;
import gr.hua.www.v2of2.charts.OSActivity;
import gr.hua.www.v2of2.charts.ProvidersActivity;
import gr.hua.www.v2of2.charts.StatisticsActivity;
import gr.hua.www.v2of2.charts.VendorsActivity;

public class ChartsActivity extends BaseActivity {

    BoomMenuButton bmb;
    ArrayList<Integer> imageIDList;
    ArrayList<String> titleList;
    public int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        bmb = (BoomMenuButton) findViewById(R.id.boom);
        imageIDList = new ArrayList<>();
        titleList = new ArrayList<>();
        setInitialData();

        for (int i = 0; i < bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            position = i;
            TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
                    .normalImageRes(imageIDList.get(i))
                    .normalText(titleList.get(i))
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            goToPage(index);
                        }
                    });
            bmb.addBuilder(builder);

        }

    }

    private void setInitialData() {
        imageIDList.add(R.drawable.measure);
        imageIDList.add(R.drawable.os);
        imageIDList.add(R.drawable.vendors);
        imageIDList.add(R.drawable.signal);
        imageIDList.add(R.drawable.providers);

        titleList.add("Measurements");
        titleList.add("Operating Systems");
        titleList.add("Vendors");
        titleList.add("Networks");
        titleList.add("Providers");
    }

    private void goToPage(int i) {
        Intent intent;
        if (i == 0) {
            intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
        } else if (i == 1) {
            intent = new Intent(this, OSActivity.class);
            startActivity(intent);
        } else if (i == 2) {
            intent = new Intent(this, VendorsActivity.class);
            startActivity(intent);
        } else if(i==3){
            intent = new Intent(this, NetworksActivity.class);
            startActivity(intent);
        }else{
            intent = new Intent(this, ProvidersActivity.class);
            startActivity(intent);
        }

    }

}

