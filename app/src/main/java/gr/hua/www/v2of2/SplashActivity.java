package gr.hua.www.v2of2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by father on 10/7/17.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        startActivity(new Intent(SplashActivity.this, BaseActivity.class));
        finish();
    }
}