package gr.hua.www.v2of2;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

//Choose Map Type
public class MapView extends AppCompatActivity {

    public static String mapv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_hybrid:
                if (checked)
                    mapv = "h";
                break;
            case R.id.radio_normal:
                if (checked)
                    mapv = "n";
                break;
            case R.id.radio_satellite:
                if (checked)
                    mapv = "s";
                break;
            case R.id.radio_terrain:
                if (checked)
                    mapv = "t";
                break;
        }

        this.finish(); //*for closing the window after check*
    }

}
