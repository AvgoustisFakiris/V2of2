package gr.hua.www.v2of2;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

//Show Debugging from Login Page
public class DebugActivity extends AppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        Bundle b=getIntent().getExtras();
        tv = (TextView) findViewById(R.id.tvSecond);
        String res=b.getString("resp");
        tv.setText(res);
    }


}
