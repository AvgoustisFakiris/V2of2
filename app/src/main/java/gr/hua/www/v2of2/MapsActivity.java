package gr.hua.www.v2of2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static gr.hua.www.v2of2.MapView.mapv;


public class MapsActivity extends BaseActivity implements
        OnMapReadyCallback {

    private final String TAG = getClass().getName(); // logging purposes
    private Bitmap b;
    private ArrayList<LatLng> mPointList = new ArrayList<>();

    GoogleMap map;

    private String jsonTuple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(TAG, "onLocationResult Maps");
                        Log.d(TAG, "Lat: " + location.getLatitude() + " Lng : " + location.getLongitude());
                        //   point = new LatLng(location.getLatitude(), location.getLongitude());
                        //   mLastLocation = mCurrentLocation;
                        //   mCurrentLocation = location;
                       // mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                       // mSpeed = 3.6f * mCurrentLocation.getSpeed(); // -> Km/h
                        //  getInfo();
                        if (point != null) {
                            addPoint();
                        }

                    }
                }
            }
        };
        Log.d(TAG, "onCreate .Override......................");
    }


    @Override
    public void onStart() {
        Log.d(TAG, "onStart .Override......................");
        super.onStart();
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

    @Override
    public void onStop() {
        Log.d(TAG, "onStop .Override......................");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy .Override......................");
        super.onDestroy();
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        try {
            if (FLAG_KEEP_SCREEN_ON == 1) {//If flag is on
                getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        Log.d(TAG, "onConnected - isConnected .Override......: " + mGoogleApiClient.isConnected());
        // Try to obtain the map from the SupportMapFragment.
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        fm.getMapAsync(this);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged inside ........");
        addPoint();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        map = googleMap;
        //Check Map type
        if (mapv == "t") {
            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (mapv == "n") {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (mapv == "s") {
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.getUiSettings().setAllGesturesEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.setIndoorEnabled(true);
            map.setBuildingsEnabled(true);
            map.setMyLocationEnabled(true);

        }
        // Change color and icon of the default marker
        Bitmap ob = BitmapFactory.decodeResource(this.getResources(), R.drawable.point);
        b = Bitmap.createBitmap(ob.getWidth(), ob.getHeight(), ob.getConfig());
        Canvas canvas = new Canvas(b);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(ob, 0f, 0f, paint);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Adding the mPointList arraylist to Bundle
        outState.putParcelableArrayList("points", mPointList);
        // Saving the bundle
        super.onSaveInstanceState(outState);
    }


    public void addPoint() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        if (mCurrentLocation != null && mLastLocation != null) {
            // Adding point to database
            if (TOKEN != null) {
                //  writeTuple();
            }
            // float bearing = mLastLocation.bearingTo(mCurrentLocation);
            float bearing = mCurrentLocation.getBearing();
            // Creating a LatLng object for the current location
            point = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            // Creating an instance of MarkerOptions
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(b))
                    .position(point) // Setting latitude and longitude for the marker
                    .rotation(bearing)
                    .flat(true)
                    .title(jsonTuple); // Setting a title for this marker
            // Adding the currently created marker position to the arraylist
            mPointList.add(point);
            Log.d(TAG, String.valueOf(mPointList.size()));
            // Adding marker on the Google Map
            map.addMarker(markerOptions);
            // Update camera
            if (mPointList.size() == 1) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 19));
            }
            updateCameraBearing(map, point, bearing);
        } else {
            Log.d(TAG, "There is no current location!");
        }
    }


    private void updateCameraBearing(GoogleMap googleMap, LatLng point, float bearing) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        if (googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .target(point)
                .bearing(bearing)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
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
            // TOKEN = intent.getStringExtra(RestTask.TOKEN);
            Log.d(TAG, "RESPONSE = " + TOKEN);
//            addPoint();
        }
    };

}