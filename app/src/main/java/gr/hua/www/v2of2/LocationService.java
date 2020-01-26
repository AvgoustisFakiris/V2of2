package gr.hua.www.v2of2;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import static gr.hua.www.v2of2.BaseActivity.JSON;
import static gr.hua.www.v2of2.BaseActivity.REFRESHTOKEN;
import static gr.hua.www.v2of2.BaseActivity.TOKEN;
import static gr.hua.www.v2of2.BaseActivity.androidID;
import static gr.hua.www.v2of2.BaseActivity.androidOS;
import static gr.hua.www.v2of2.BaseActivity.aversion;
import static gr.hua.www.v2of2.BaseActivity.brand;
import static gr.hua.www.v2of2.BaseActivity.deviceID;
import static gr.hua.www.v2of2.BaseActivity.deviceSerial;
import static gr.hua.www.v2of2.BaseActivity.level;
import static gr.hua.www.v2of2.BaseActivity.mTelephonyManager;
import static gr.hua.www.v2of2.BaseActivity.manufactorer;
import static gr.hua.www.v2of2.BaseActivity.mcc;
import static gr.hua.www.v2of2.BaseActivity.mnc;
import static gr.hua.www.v2of2.BaseActivity.model;
import static gr.hua.www.v2of2.BaseActivity.networkType;
import static gr.hua.www.v2of2.BaseActivity.operatorName;
import static gr.hua.www.v2of2.BaseActivity.os;
import static gr.hua.www.v2of2.BaseActivity.osId;
import static gr.hua.www.v2of2.BaseActivity.pass;
import static gr.hua.www.v2of2.BaseActivity.product;
import static gr.hua.www.v2of2.BaseActivity.ssid;
import static gr.hua.www.v2of2.BaseActivity.subscriberID;
import static gr.hua.www.v2of2.BaseActivity.user;
import static gr.hua.www.v2of2.BaseActivity.point;


public class LocationService extends Service {

    private static final String TAG = "LocationService";

    protected ProgressDialog progress;
    protected String uri;

    private FusedLocationProviderClient mFusedLocationClient;
    protected static final long INTERVAL = 1000 * 1 * 5; // 5 seconds
    protected static final long FASTEST_INTERVAL = 1000 * 1 * 5; // 5 seconds

    private String jsonTuple;
    protected static Location mLastLocation;
    protected static Location mCurrentLocation;
    protected int accuracy, altitude;
    protected String mLastUpdateTime;
    protected String dtm, longi, lati;
    protected boolean serv = true;

    //Post Json variables
    private String node, event, locationsource, conntype, conninfo, versionname, androidversion, camp = "";
   /* private String appversioncode = null, dl_bitrate = null, ul_bitrate = null, nlac1 = null, ncellid2 = null , nrxlev2 = null , nlac3 = null , ncellid3 = null ,nrxlev3 = null , nlac4 = null , ncellid4 = null
            , nrxlev4 = null ,nlac5 = null , ncellid5 = null , nrxlev5 = null , nlac6 = null , ncellid6 = null ,nrxlev6 = null ,nrxlev1 = null ,nlac2 = null ,
    avgping = null, minping = null, maxping = null , stdevping = null , pingloss = null,testdlbitrate = null, testulbitrate = null,servingcelltime = null;
    private int lterssi = 0; */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        serv = true;

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
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }

        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stop Service");
        serv = false;
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

                        //Get signal Info
                        BaseActivity la = new BaseActivity();
                        la.getInfo();

                        Location location = locationResult.getLastLocation();
                        mCurrentLocation = location;
                        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                        longi = String.valueOf(mCurrentLocation.getLongitude());
                        lati = String.valueOf(mCurrentLocation.getLatitude());
                        dtm = convertTime(mCurrentLocation.getTime());
                        if (location != null) {
                            writeTuple();
                        }
                        //Create Point on Map
                        point = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }


    private void writeTuple() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        jsonTuple = "{" +
                "\"timestamp\":\"" + dtm + "\"" +
                ",\"lon\":\"" + longi.substring(0, 7) + "\"" +
                ",\"lat\":\"" + lati.substring(0, 7) + "\"" +
                ",\"level\":" + level +
                ",\"speed\":" + 3.6f * mCurrentLocation.getSpeed() +
                ",\"operatorname\":\"" + operatorName + "\"" +
                ",\"mcc\":\"" + mcc + "\"" +
                ",\"mnc\":\"" + mnc + "\"" +
                ",\"node\":\"" + "" + "\"" +
                ",\"cellid\":\"" + "" + "\"" +
                ",\"lac\":" + 0 +
                ",\"network_type\":\"" + networkType + "\"" +
                ",\"qual\":" + 0 +
                ",\"snr\":" + 0 +
                ",\"cqi\":" + 0 +
                ",\"lterssi\":" + 0 +
                ",\"appversioncode\":" + 0 +
                ",\"psc\":" + 0 +
                ",\"dl_bitrate\":" + 0 +
                ",\"ul_bitrate\":" + 0 +
                ",\"nlac1\":" + 0 +
                ",\"ncellid1\":" + 0 +
                ",\"nrxlev1\":" + 0 +
                ",\"nlac2\":" + 0 +
                ",\"ncellid2\":" + 0 +
                ",\"nrxlev2\":" + 0 +
                ",\"nlac3\":" + 0 +
                ",\"ncellid3\":" + 0 +
                ",\"nrxlev3\":" + 0 +
                ",\"nlac4\":" + 0 +
                ",\"ncellid4\":" + 0 +
                ",\"nrxlev4\":" + 0 +
                ",\"nlac5\":" + 0 +
                ",\"ncellid5\":" + 0 +
                ",\"nrxlev5\":" + 0 +
                ",\"nlac6\":" + 0 +
                ",\"ncellid6\":" + 0 +
                ",\"nrxlev6\":" + 0 +
                ",\"ctime\":\"" + dtm + "\"" +
                ",\"event\":\"" + "" + "\"" +
                ",\"accuracy\":" + (int) mCurrentLocation.getAccuracy() +
                ",\"locationsource\":\"" + "G" + "\"" +
                ",\"altitude\":" + (int) mCurrentLocation.getAltitude() +
                ",\"conntype\":\"" + conntype + "\"" +
                ",\"conninfo\":\"" + conninfo + "\"" +
                ",\"avgping\":" + null +
                ",\"minping\":" + null +
                ",\"maxping\":" + null +
                ",\"stdevping\":" + null +
                ",\"pingloss\":" + null +
                ",\"testdlbitrate\":" + null +
                ",\"testulbitrate\":" + null +
                ",\"geomcol2\":" + null +
                ",\"devicemanufacturer\":\"" + manufactorer + "\"" +
                ",\"devicemodel\":\"" + model + "\"" +
                ",\"devicename\":\"" + product + "\"" +
                ",\"versionname\":\"" + androidOS + "\"" +
                ",\"brand\":\"" + brand + "\"" +
                ",\"androidversion\":\"" + aversion + "\"" +
                ",\"servingcelltime\":" + null +
                ",\"os\":\"" + os + "\"" +
                ",\"os_id\":\"" + osId + "\"" +
                ",\"ssid\":" + ssid +
                ",\"camp\":\"" + "" + "\"" +
                "}";
        try {
            if (serv) {
                sendTuple(jsonTuple);
            }
            Log.d("jsonTuple", jsonTuple);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTuple(final String json) throws IOException {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // the request
        OkHttpClient client = new OkHttpClient();

        uri = "http://test.hua.gr/v2of/myapp/measurements/";
        String credential = Credentials.basic(user, pass);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("Authorization", "JWT " + TOKEN)
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String myResponse = response.body().string();
                Log.d("Response", myResponse);
                //Check if token expired
                if (response.code() == 401) {
                    //Refresh Token
                    TOKEN = REFRESHTOKEN;
                }

            }

        });
    }

    //DateTime format of database
    public String convertTime(long time) {
        Date date = new Date(time);
        Format format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return format.format(date);
    }


}