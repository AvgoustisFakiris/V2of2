package gr.hua.www.v2of2;

/**
 * Created by father on 14/4/17.
 */

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.http.client.methods.HttpPost;

import java.net.NetworkInterface;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.hua.www.v2of2.receivers.DeviceReceiver;
import gr.hua.www.v2of2.utils.CustomPhoneStateListener;
import gr.hua.www.v2of2.utils.Installation;
import gr.hua.www.v2of2.utils.RestTask;
import gr.hua.www.v2of2.utils.StickyService;
import okhttp3.MediaType;


/**
 * Handles the permission requests for application.
 * Checks for Google services availability.
 * Connects with GoogleApiClient and creates a location request.
 */
public class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    protected static final long INTERVAL = 1000 * 60 * 1; // 1 minute
    protected static final long FASTEST_INTERVAL = 1000 * 1 * 5; // 5 seconds
    protected static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 0x1;
    protected static final int PERMISSIONS_REQUEST_GPS = 0x5;
    protected static final int PERMISSIONS_MULTIPLE = 0x6;
    protected static final String ACTION_FOR_INTENT_CALLBACK = "THIS_IS_A_UNIQUE_KEY_WE_USE_TO_COMMUNICATE";
    protected static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static DeviceReceiver ncr;
    public static boolean passiveEnabled;
    public static boolean gpsEnabled;
    public static boolean networkEnabled;
    public static boolean wifiEnabled;
    public static boolean mobileEnabled;
    public static boolean permissionsGranted;
    public static boolean ncrRegistered = false;
    public static boolean gacConnected = false;
    public static boolean mRequestingLocationUpdates = false;
    public static Installation install;
    public static String deviceID;
    public static String subscriberID;
    public static String macID;
    public static String androidID;
    public static String deviceSerial;
    public static String U2ID;
    public static String model;
    public static String brand;
    public static String product;
    public static String os;
    public static String osId;
    public static int level;
    public static long cid;
    public static long lac;
    public static long psc;
    public static int qual;
    public static int cqi;
    public static int snr;
    public static float mSpeed;
    public static String networkType;
    public static String operatorName;
    public static String mcc;
    public static String mnc;
    protected static String URL = "https://test.hua.gr:8443/HuaTester";
    protected static String TOKEN;
    protected static TelephonyManager mTelephonyManager;
    static String REQUESTING_LOCATION_UPDATES_KEY;
    private static View v;
    private final String TAG = getClass().getName(); // logging purposes
    /* Client used to interact with Google APIs. */
    protected int GooglePlayAvailability;
    protected GoogleApiClient mGoogleApiClient;
    protected FusedLocationProviderClient mFusedLocationClient;
    protected LocationCallback mLocationCallback;
    protected LocationRequest mLocationRequest;
    protected PendingResult<LocationSettingsResult> result;
    protected static Location mCurrentLocation;
    protected static Location mLastLocation;
    protected LatLng point;
    protected String mLastUpdateTime;
    protected ProgressDialog progress;
    protected String user;
    protected String pass;
    protected String uri;

    public static void setSNR(int snr) {
        BaseActivity.snr = snr;
    }

    public static void setNetworkType(int n) {
        BaseActivity.networkType = getNetworkTech(n);
    }

    public static void setQual(int qual) {
        BaseActivity.qual = qual;
    }

    public static void setCqi(int cqi) {
        BaseActivity.cqi = cqi;
    }

    public static void setCid(long cid) {
        BaseActivity.cid = cid;
    }

    public static void setLac(long lac) {
        BaseActivity.lac = lac;
    }

    public static void setPsc(long psc) {
        BaseActivity.psc = psc;
    }

    public static void setLevel(int level) {
        BaseActivity.level = level;
    }

    /**
     * Show an {@link android.app.AlertDialog} with an 'OK' button and a message.
     *
     * @param activity the Activity in which the Dialog should be displayed.
     * @param message  the message to display in the Dialog.
     */
    public static void showAlert(Activity activity, String message) {
        (new AlertDialog.Builder(activity)).setMessage(message)
                .setNeutralButton(android.R.string.ok, null).create().show();
    }

    /**
     * Resolve a connection failure from
     * {@link com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener#onConnectionFailed(com.google.android.gms.common.ConnectionResult)}
     *
     * @param activity             the Activity trying to resolve the connection failure.
     * @param client               the GoogleAPIClient instance of the Activity.
     * @param result               the ConnectionResult received by the Activity.
     * @param requestCode          a request code which the calling Activity can use to identify the result
     *                             of this resolution in onActivityResult.
     * @param fallbackErrorMessage a generic error message to display if the failure cannot be resolved.
     * @return true if the connection failure is resolved, false otherwise.
     */
    public static boolean resolveConnectionFailure(Activity activity,
                                                   GoogleApiClient client,
                                                   ConnectionResult result,
                                                   int requestCode,
                                                   String fallbackErrorMessage) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
                return true;
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                client.connect();
                return false;
            }
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            } else {
                // no built-in dialog: show the fallback error message
                showAlert(activity, fallbackErrorMessage);
            }
            return false;
        }
    }

    public static String getNetworkTech(int n) {
        if (n == 7) {
            return "2G";
        }
        if (n == 4) {
            return "2G";
        }
        if (n == 2) {
            return "2G";
        }
        if (n == 14) {
            return "3G";
        }
        if (n == 5) {
            return "3G";
        }
        if (n == 6) {
            return "3G";
        }
        if (n == 12) {
            return "3G";
        }
        if (n == 1) {
            return "2G";
        }
        if (n == 8) {
            return "3G";
        }
        if (n == 10) {
            return "3G";
        }
        if (n == 15) {
            return "3G";
        }
        if (n == 11) {
            return "2G";
        }
        if (n == 13) {
            return "4G";
        }
        if (n == 3) {
            return "3G";
        }
        if (n != 0) return "NONE";
        return "--";
    }

    public void setOperatorName(String operatorname) {
        this.operatorName = operatorname;
    }

    public void setMCC(String mcc) {
        this.mcc = mcc;
    }

    public void setMNC(String mnc) {
        this.mnc = mnc;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        try {
            updateValuesFromBundle(savedInstanceState);
            Intent stickyService = new Intent(this, StickyService.class);
            startService(stickyService);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate error...............................");
        }
        if (Build.VERSION.SDK_INT < 23) {
            //Do not need to check the permission
        } else {
            //show error dialog if GooglePlayServices are not available
            if (!isGooglePlayServicesAvailable())
                finish();

            buildGoogleApiClient();

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.d(TAG, new Object() {
                    }.getClass().getEnclosingMethod().getName());
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            Log.d(TAG, "onLocationResult inside");
                            Log.d(TAG, "Lat: " + location.getLatitude() + " Lng : " + location.getLongitude());
                            point = new LatLng(location.getLatitude(), location.getLongitude());
                            mCurrentLocation = location;
                            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                        }
                    }
                }
            };
        }
        if (!ncrRegistered) {
            try {
                ncr = new DeviceReceiver();
                IntentFilter ifr = new IntentFilter();
                ifr.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                ifr.addAction("android.location.PROVIDERS_CHANGED");
                this.registerReceiver(ncr, ifr); //Register the Network Change Receiver
                ncrRegistered = true;
                Log.d(TAG, "DeviceReceiver registered");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }
        }
        // Update UI to match restored state
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    protected boolean isGooglePlayServicesAvailable() {
        try {
            // Check for Google Play Services and install if it is possible
            GooglePlayAvailability = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (ConnectionResult.SUCCESS == GooglePlayAvailability) {
                //Toast.makeText(getBaseContext(), "Google Play is Available!", Toast.LENGTH_LONG).show();
                Log.d(TAG, "isGooglePlayServicesAvailable Succeed!");
                return true;
            } else {
                Toast.makeText(getBaseContext(), "Google Play is NOT Available!", Toast.LENGTH_LONG).show();
                if (GooglePlayServicesUtil.isUserRecoverableError(GooglePlayAvailability)) {
                    GooglePlayServicesUtil.getErrorDialog(GooglePlayAvailability, this, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
                } else {
                    Toast.makeText(getBaseContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                    finish();
                }
                Log.d(TAG, "isGooglePlayServicesAvailable Failed!");
                return false;
            }
            // Create a UUID per installation
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "isGooglePlayServicesAvailable Failed!");
            return false;
        }
    }

    protected boolean checkPermissions() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        try {
            List<String> permissionsNeeded = new ArrayList<>();
            final List<String> permissionsList = new ArrayList<>();
            if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissionsNeeded.add("GPS");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                permissionsNeeded.add("Coarse Location");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionsNeeded.add("Write External Storage");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionsNeeded.add("Read External Storage");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.READ_PHONE_STATE)) {
                permissionsNeeded.add("Phone State");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.INTERNET)) {
                permissionsNeeded.add("Internet");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_NETWORK_STATE)) {
                permissionsNeeded.add("Network State");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)) {
                permissionsNeeded.add("Location Extra");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                permissionsNeeded.add("Notification Policy");
            }
            if (!addPermission(permissionsList, android.Manifest.permission.CHANGE_WIFI_STATE)) {
                permissionsNeeded.add("Change Wifi State");
            }
            if (permissionsList.size() > 0) {
                Log.d(TAG, "List size:" + String.valueOf(permissionsList.size()));
                Log.d(TAG, "Needed size:" + String.valueOf(permissionsNeeded.size()));
                if (permissionsNeeded.size() > 0) {
                    Log.d(TAG, "checkPermissions ..step2........................");
                    // Need Rationale
                    String message = "You need to grant access to " + permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        message = message + ", " + permissionsNeeded.get(i);
                    showMessageOKCancel(message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (dialog != null) {
                                        Log.d(TAG, "checkPermissions ..step3........................");
                                        ActivityCompat.requestPermissions(BaseActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                                PERMISSIONS_MULTIPLE);
                                    }
                                }
                            });
                    Log.d(TAG, "checkPermissions ..step2........................" + permissionsGranted);
                    return false;
                }
                ActivityCompat.requestPermissions(BaseActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                        PERMISSIONS_MULTIPLE);
                Log.d(TAG, "checkPermissions .........................." + permissionsGranted);
                return false;
            }
            // Toast.makeText(BaseActivity.this, "All required permissions have been granted!", Toast.LENGTH_SHORT).show();
            permissionsGranted = true;
            Log.d(TAG, "checkPermissions .........................." + permissionsGranted);
            return permissionsGranted;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "isGooglePlayServicesAvailable Failed!");
            return false;
        }
    }

    /**
     * Check if appropriate permissions have been granted.
     */
    public void btnStart(View view) {
        v = view;
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        getMeasurements();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void btnLogin(View view) {
        v = view;
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Handles the result of the request for application settings panel.
     */
    public void btnSettings(View view) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Handles the result of the request for exit.
     */
    public void btnExit(View view) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BaseActivity.this.finishAffinity();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Handles the result of the request for permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());

        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSIONS_MULTIPLE: {
                    Log.d(TAG, "onRequestPermissionsResult PERMISSIONS_MULTIPLEPERMISSIONS_MULTIPLE ...");
                    Map<String, Integer> perms = new HashMap<String, Integer>();
                    // Initial
                    perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.ACCESS_NETWORK_STATE, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.ACCESS_WIFI_STATE, PackageManager.PERMISSION_GRANTED);
                    perms.put(android.Manifest.permission.CHANGE_WIFI_STATE, PackageManager.PERMISSION_GRANTED);

                    // Fill with results
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for Permissions
                    if (perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
                            ) {
                        // All Permissions Granted
                        Toast.makeText(BaseActivity.this, "Thank you!", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        // Permission Denied
                        Toast.makeText(BaseActivity.this, "A required permission is denied", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
            //Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            checkPermissions();
        } else {
            Toast.makeText(this, "A required permission is denied", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Open settings to grant permission or exit!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            try {
                if (!permissionsGranted)
                    checkPermissions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    @Override
    public void onStop() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        try {
            if (mGoogleApiClient != null) {
                if (mRequestingLocationUpdates)
                    stopLocationUpdates();
                mGoogleApiClient.disconnect();
                gacConnected = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        try {
            if (ncrRegistered) {//If Network Change Receiver is registered
                this.unregisterReceiver(ncr);
                ncrRegistered = false;
                Log.d(TAG, "unregisterReceiver succeed!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.gc();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        gacConnected = true;
        createLocationRequest();
        getLastKnownLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
        resolveConnectionFailure(this, mGoogleApiClient, connectionResult, 0, "Google API connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        if (location != null) {
            Log.d(TAG, "Lat: " + location.getLatitude() + " Lng : " + location.getLongitude());
            point = new LatLng(location.getLatitude(), location.getLongitude());
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        // Saving the bundle
        super.onSaveInstanceState(outState);
    }

    protected boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "addPermission ..............................." + permission);
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                return false;
            else
                return false;
        }
        return true;
    }

    public void askForGPS() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(BaseActivity.this, PERMISSIONS_REQUEST_GPS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    public void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        new AlertDialog.Builder(BaseActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
        mRequestingLocationUpdates = true;
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    protected void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mRequestingLocationUpdates = false;
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    protected void createLocationRequest() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void getLastKnownLocation() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            Log.d(TAG, "getLastKnownLocation succeed!!");
                        }
                    }
                });
    }

    public String getMacAddress() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    protected void getMeasurements() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

    /*
    * getDeviceId() returns the unique device ID.
    * For example,the IMEI for GSM and the MEID or ESN for CDMA phones.
    */
        deviceID = mTelephonyManager.getDeviceId();
        if (deviceID.equals(null)) {
            deviceID = "0000000000000000";
        }
    /*
    * getSubscriberId() returns the unique subscriber ID,
    * For example, the IMSI for a GSM phone.
    */
        subscriberID = mTelephonyManager.getSubscriberId();
        if (subscriberID.equals(null)) {
            subscriberID = "0000000000000000";
        }
    /*
    * getMacAddress() returns the MAC,
    */
        macID = getMacAddress();
    /*
    * Returns the unique android ID.
    */
        androidID = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (androidID.equals(null)) {
            androidID = "0000000000000000";
        }

        deviceSerial = (String) Build.SERIAL;
        if (deviceSerial.equals(null)) {
            deviceSerial = "0000000000000000";
        }

        U2ID = install.id(this);

        model = Build.MODEL;
        brand = Build.BRAND;
        product = Build.PRODUCT;

        os = Build.VERSION.BASE_OS;
        osId = Build.VERSION.RELEASE;

        setMCC(this.mTelephonyManager.getNetworkOperator().substring(0, 3));
        setMNC(this.mTelephonyManager.getNetworkOperator().substring(3));
        setOperatorName(this.mTelephonyManager.getNetworkOperatorName());

        Log.i(TAG, brand);
        Log.i(TAG, model);
        Log.i(TAG, product);
        Log.i(TAG, Build.ID);
        Log.i(TAG, operatorName);
        Log.i(TAG, Build.VERSION.BASE_OS);
        Log.i(TAG, Build.VERSION.RELEASE);
        Log.i(TAG, "UUID : " + U2ID);
        Log.i(TAG, "Android ID : " + androidID);
        Log.i(TAG, "Device ID : " + deviceID);
        Log.i(TAG, "Device Serial : " + deviceSerial);
        Log.i(TAG, "Subscriber ID : " + subscriberID);
        Log.i(TAG, "Device MAC : " + macID);

    }

    protected void getInfo() {
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());

        // mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        mTelephonyManager.listen(new CustomPhoneStateListener(this),
                PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                        | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                        | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);

        setNetworkType(mTelephonyManager.getNetworkType());

    }

    protected void getToken() {
        // Next lines are only for testing purposes
        user = "test";
        pass = "1234";
        // the request
        try {
            uri = URL + "/login/?username=" + user + "&password=" + pass;
            HttpPost httpPost = new HttpPost(new URI(uri));
            RestTask task = new RestTask(this, ACTION_FOR_INTENT_CALLBACK);
            task.execute(httpPost);
            progress = ProgressDialog.show(this, "Authenticating ...", "Getting Token ...", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }
}