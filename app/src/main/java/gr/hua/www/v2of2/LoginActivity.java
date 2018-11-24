package gr.hua.www.v2of2;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import gr.hua.www.v2of2.utils.RestTask;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by father on 4/8/17.
 */

public class LoginActivity extends BaseActivity {

    private final String TAG = getClass().getName(); // logging purposes
    private TextView tv;


    /**
     * Any code to access activity fields must be handled in this method.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tv = (TextView) findViewById(R.id.tvFragSecond);
        // Initiate the request to the protected service
        final Button submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                EditText editText = (EditText) getActivity().findViewById(R.id.username);
//                user = editText.getText().toString();
                user = "test";
//                editText = (EditText) getActivity().findViewById(R.id.password);
//                pass = editText.getText().toString();
                pass = "1234";
                uri = URL + "/login/?username=" + user + "&password=" + pass;
                Log.i(TAG, uri);
                getToken();
            }
        });
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }


    @Override
    public void onResume() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        super.onResume();
        this.registerReceiver(receiver, new IntentFilter(ACTION_FOR_INTENT_CALLBACK));
    }

    @Override
    public void onPause() {
        this.unregisterReceiver(receiver);
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        super.onPause();
    }


    /**
     * Our Broadcast Receiver. We get notified that the data is ready this way.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, new Object() {
            }.getClass().getEnclosingMethod().getName());
            // clear the progress indicator
            if (progress != null) {
                progress.dismiss();
            }
            //String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
            TOKEN = intent.getStringExtra(RestTask.TOKEN);
            tv.setText(TOKEN);
            Log.d(TAG, "RESPONSE = " + TOKEN);
            try {
                listAllCampaigns();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public void listAllCampaigns() throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();

        uri = URL + "/api/campaign/";
        Request request = new Request.Builder()
                .url(uri)
                .get()
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        progress = ProgressDialog.show(this, "Please wait ...", "data are loading ...", true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (progress != null) {
                    progress.dismiss();
                }
                final String myResponse = response.body().string();
                Log.i(TAG, myResponse);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(myResponse);
                    }
                });
            }
        });
    }


}