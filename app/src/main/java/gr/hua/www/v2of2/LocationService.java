package gr.hua.www.v2of2;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

import android.content.BroadcastReceiver;


import gr.hua.www.v2of2.utils.CustomPhoneStateListener;
import gr.hua.www.v2of2.utils.Installation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.net.Uri;

import java.net.URI;

import static com.google.android.gms.internal.zzagz.runOnUiThread;
import static gr.hua.www.v2of2.BaseActivity.JSON;
import static gr.hua.www.v2of2.BaseActivity.TOKEN;
import static gr.hua.www.v2of2.BaseActivity.URL;
import static gr.hua.www.v2of2.BaseActivity.brand;
import static gr.hua.www.v2of2.BaseActivity.cid;
import static gr.hua.www.v2of2.BaseActivity.cqi;
import static gr.hua.www.v2of2.BaseActivity.lac;
import static gr.hua.www.v2of2.BaseActivity.level;
import static gr.hua.www.v2of2.BaseActivity.mCurrentLocation;
import static gr.hua.www.v2of2.BaseActivity.mSpeed;
import static gr.hua.www.v2of2.BaseActivity.mTelephonyManager;
import static gr.hua.www.v2of2.BaseActivity.mcc;
import static gr.hua.www.v2of2.BaseActivity.mnc;
import static gr.hua.www.v2of2.BaseActivity.model;
import static gr.hua.www.v2of2.BaseActivity.networkType;
import static gr.hua.www.v2of2.BaseActivity.operatorName;
import static gr.hua.www.v2of2.BaseActivity.osId;
import static gr.hua.www.v2of2.BaseActivity.product;
import static gr.hua.www.v2of2.BaseActivity.psc;
import static gr.hua.www.v2of2.BaseActivity.qual;
import static gr.hua.www.v2of2.BaseActivity.setNetworkType;
import static gr.hua.www.v2of2.BaseActivity.snr;


public class LocationService extends Service {

    private static final String TAG = "LocationService";

    protected ProgressDialog progress;
    protected String uri;

    private FusedLocationProviderClient mFusedLocationClient;
    protected static final long INTERVAL = 1000 * 60 * 1; // 1 minute
    protected static final long FASTEST_INTERVAL = 1000 * 1 * 5; // 5 seconds

    private String jsonTuple;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getLocation() {

        // ---------------------------------- LocationRequest ------------------------------------
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "onLocationResult: got location result.");

                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            writeTuple();
                        }
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }


    private void writeTuple() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        jsonTuple = "{" +
                "\"brand\":\"" + brand + "\"" +
                ",\"deviceModel\":\"" + model + "\"" +
                ",\"deviceName\":\"" + product + "\"" +
//                    ",\"os\":\"" + os + "\"" +
                ",\"osId\":\"" + osId + "\"" +
                ",\"operatorname\":\"" + operatorName + "\"" +
                ",\"networkType\":\"" + networkType + "\"" +
                ",\"lat\":" + mCurrentLocation.getLatitude() +
                ",\"lon\":" + mCurrentLocation.getLongitude() +
                ",\"speed\":" + 3.6f * mCurrentLocation.getSpeed() +
                ",\"accuracy\":" + mCurrentLocation.getAccuracy() +
                ",\"altitude\":" + mCurrentLocation.getAltitude() +
                ",\"timestamp\":" + mCurrentLocation.getTime() +
                ",\"level\":" + level +
                ",\"cellid\":" + cid +
                ",\"lac\":" + lac +
                ",\"psc\":" + psc +
                ",\"qual\":" + qual +
                ",\"cqi\":" + cqi +
                ",\"mcc\":\"" + mcc + "\"" +
                ",\"mnc\":\"" + mnc + "\"" +
                ",\"snr\":" + snr +
                "}";
        try {
            sendTuple(jsonTuple);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTuple(final String json) throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();

        uri = URL + "/api/measurement/";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("token", TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

//       progress = ProgressDialog.show(this, "Please wait ...", "saving data ...", true);
//        Toast.makeText(getBaseContext(), "Transmit data!", Toast.LENGTH_LONG).show();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
/*
                if (progress != null) {
                    progress.dismiss();
                }
*/
                String myResponse = response.body().string();
                Log.d(TAG, myResponse);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "sendTuple: " + json.toString());
//                        Log.d(TAG, "TOKEN: " + TOKEN);
                    }
                });
            }
        });
    }

}