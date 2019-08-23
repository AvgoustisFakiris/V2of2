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
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
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
import android.support.v7.widget.CardView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

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


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.lang.reflect.Method;
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
import gr.hua.www.v2of2.utils.VersionHelper;
import okhttp3.MediaType;


/**
 * Handles the permission requests for application.
 * Checks for Google services availability.
 * Connects with GoogleApiClient and creates a location request.
 */
public class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener, View.OnClickListener {

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
    public static boolean locationModeEnable;
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
    protected static String URL = "http://test.hua.gr/v2of";
    protected static String TOKEN;
    protected static TelephonyManager mTelephonyManager;
    static String REQUESTING_LOCATION_UPDATES_KEY;
    private static View v;
    private CardView driveCard, settingsCard, chartsCard, privacyCard, loginCard; //dashboard purposes
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

    public static String jsonT;
    public static boolean dbg = false; //debugging choice
    public static boolean snd = false; //Sound choice

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
        operatorName = operatorname;
    }

    public void setMCC(String mcc) {
        BaseActivity.mcc = mcc;
    }

    public void setMNC(String mnc) {
        BaseActivity.mnc = mnc;
    }

    //Privacy Policy Message
    private String myvar = "Effective date: October 12, 2018\n" +
            "\n" +
            "V2OF . This SERVICE is provided by Dr. Vassilis Dalakas - Avgoustis Fakiris as a Diploma Project at no cost and is intended for use as is. \n" +
            "\n" +
            "This page informs you of our policies regarding the collection, use, and disclosure of personal data when you use our Service and the choices you have associated with that data. Our Privacy Policy for V2OF is managed through Free Privacy Policy.\n" +
            "\n" +
            "We use your data to provide and improve the Service. By using the Service, you agree to the collection and use of information in accordance with this policy. Unless otherwise defined in this Privacy Policy, terms used in this Privacy Policy have the same meanings as in our Terms and Conditions.\n" +
            "\n" +
            "Information Collection And Use\n" +
            "We collect several different types of information for various purposes to provide and improve our Service to you.\n" +
            "\n" +
            "Types of Data Collected\n" +
            "Personal Data\n" +
            "While using our Service, we may ask you to provide us with certain personally identifiable information that can be used to contact or identify you (\"Personal Data\"). Personally identifiable information may include, but is not limited to:\n" +
            "\n" +
            "Email address\n" +
            "Address, State, Province, ZIP/Postal code, City\n" +
            "Cookies and Usage Data\n" +
            "Usage Data\n" +
            "We may also collect information that your browser sends whenever you visit our Service or when you access the Service by or through a mobile device (\"Usage Data\").\n" +
            "\n" +
            "This Usage Data may include information such as your computer's Internet Protocol address (e.g. IP address), browser type, browser version, the pages of our Service that you visit, the time and date of your visit, the time spent on those pages, unique device identifiers and other diagnostic data.\n" +
            "\n" +
            "When you access the Service by or through a mobile device, this Usage Data may include information such as the type of mobile device you use, your mobile device unique ID, the IP address of your mobile device, your mobile operating system, the type of mobile Internet browser you use, unique device identifiers and other diagnostic data.\n" +
            "\n" +
            "Tracking & Cookies Data\n" +
            "We use cookies and similar tracking technologies to track the activity on our Service and hold certain information.\n" +
            "\n" +
            "Cookies are files with small amount of data which may include an anonymous unique identifier. Cookies are sent to your browser from a website and stored on your device. Tracking technologies also used are beacons, tags, and scripts to collect and track information and to improve and analyze our Service.\n" +
            "\n" +
            "You can instruct your browser to refuse all cookies or to indicate when a cookie is being sent. However, if you do not accept cookies, you may not be able to use some portions of our Service.\n" +
            "\n" +
            "Examples of Cookies we use:\n" +
            "\n" +
            "Session Cookies. We use Session Cookies to operate our Service.\n" +
            "Preference Cookies. We use Preference Cookies to remember your preferences and various settings.\n" +
            "Security Cookies. We use Security Cookies for security purposes.\n" +
            "Use of Data\n" +
            "V2OF uses the collected data for various purposes:\n" +
            "\n" +
            "To provide and maintain the Service\n" +
            "To notify you about changes to our Service\n" +
            "To allow you to participate in interactive features of our Service when you choose to do so\n" +
            "To provide customer care and support\n" +
            "To provide analysis or valuable information so that we can improve the Service\n" +
            "To monitor the usage of the Service\n" +
            "To detect, prevent and address technical issues\n" +
            "Transfer Of Data\n" +
            "Your information, including Personal Data, may be transferred to — and maintained on — computers located outside of your state, province, country or other governmental jurisdiction where the data protection laws may differ than those from your jurisdiction.\n" +
            "\n" +
            "If you are located outside Greece and choose to provide information to us, please note that we transfer the data, including Personal Data, to Greece and process it there.\n" +
            "\n" +
            "Your consent to this Privacy Policy followed by your submission of such information represents your agreement to that transfer.\n" +
            "\n" +
            "V2OF will take all steps reasonably necessary to ensure that your data is treated securely and in accordance with this Privacy Policy and no transfer of your Personal Data will take place to an organization or a country unless there are adequate controls in place including the security of your data and other personal information.\n" +
            "\n" +
            "Disclosure Of Data\n" +
            "Legal Requirements\n" +
            "V2OF may disclose your Personal Data in the good faith belief that such action is necessary to:\n" +
            "\n" +
            "To comply with a legal obligation\n" +
            "To protect and defend the rights or property of V2OF\n" +
            "To prevent or investigate possible wrongdoing in connection with the Service\n" +
            "To protect the personal safety of users of the Service or the public\n" +
            "To protect against legal liability\n" +
            "Security Of Data\n" +
            "The security of your data is important to us, but remember that no method of transmission over the Internet, or method of electronic storage is 100% secure. While we strive to use commercially acceptable means to protect your Personal Data, we cannot guarantee its absolute security.\n" +
            "\n" +
            "Service Providers\n" +
            "We may employ third party companies and individuals to facilitate our Service (\"Service Providers\"), to provide the Service on our behalf, to perform Service-related services or to assist us in analyzing how our Service is used.\n" +
            "\n" +
            "These third parties have access to your Personal Data only to perform these tasks on our behalf and are obligated not to disclose or use it for any other purpose.\n" +
            "\n" +
            "Analytics\n" +
            "We may use third-party Service Providers to monitor and analyze the use of our Service.\n" +
            "\n" +
            "Firebase\n" +
            "\n" +
            "Firebase is analytics service provided by Google Inc.\n" +
            "\n" +
            "You may opt-out of certain Firebase features through your mobile device settings, such as your device advertising settings or by following the instructions provided by Google in their Privacy Policy: https://policies.google.com/privacy?hl=en\n" +
            "\n" +
            "We also encourage you to review the Google's policy for safeguarding your data: https://support.google.com/analytics/answer/6004245. For more information on what type of information Firebase collects, please visit please visit the Google Privacy & Terms web page: https://policies.google.com/privacy?hl=en\n" +
            "\n" +
            "Links To Other Sites\n" +
            "Our Service may contain links to other sites that are not operated by us. If you click on a third party link, you will be directed to that third party's site. We strongly advise you to review the Privacy Policy of every site you visit.\n" +
            "\n" +
            "We have no control over and assume no responsibility for the content, privacy policies or practices of any third party sites or services.\n" +
            "\n" +
            "Children's Privacy\n" +
            "Our Service does not address anyone under the age of 18 (\"Children\").\n" +
            "\n" +
            "We do not knowingly collect personally identifiable information from anyone under the age of 18. If you are a parent or guardian and you are aware that your Children has provided us with Personal Data, please contact us. If we become aware that we have collected Personal Data from children without verification of parental consent, we take steps to remove that information from our servers.\n" +
            "\n" +
            "Changes To This Privacy Policy\n" +
            "We may update our Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page.\n" +
            "\n" +
            "We will let you know via email and/or a prominent notice on our Service, prior to the change becoming effective and update the \"effective date\" at the top of this Privacy Policy.\n" +
            "\n" +
            "You are advised to review this Privacy Policy periodically for any changes. Changes to this Privacy Policy are effective when they are posted on this page.\n" +
            "\n" +
            "Contact Us\n" +
            "If you have any questions about this Privacy Policy, please contact us:\n" +
            "\n" +
            "By email: vdalakas@hua.gr , avg.fakiris@gmail.com";

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

            // Show the Logo icon in action bar.
            setContentView(R.layout.activity_base);

            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.ic_launcher);

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

        //Define DashBoard Cards
        driveCard = (CardView) findViewById(R.id.drivecardId);
        settingsCard = (CardView) findViewById(R.id.settingscardId);
        chartsCard = (CardView) findViewById(R.id.chartscardId);
        loginCard = (CardView) findViewById(R.id.logincardId);
        privacyCard = (CardView) findViewById(R.id.privacycardId);

        //Add clickListener to the cards
        driveCard.setOnClickListener(this);
        settingsCard.setOnClickListener(this);
        loginCard.setOnClickListener(this);
        chartsCard.setOnClickListener(this);
        privacyCard.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Debug Buttons Show
        MenuItem menuItemDebugOn = menu.findItem(R.id.menu_debug);
        MenuItem menuItemDebugOff = menu.findItem(R.id.menu_debug_off);
        //Sound Buttons Show
        MenuItem menuItemSoundOn = menu.findItem(R.id.menu_volume);
        MenuItem menuItemSoundOff = menu.findItem(R.id.menu_mute);

        //Show Icons in Overflow Menu
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        //Showing only one button at a time
        if (dbg) {
            menuItemDebugOn.setEnabled(false).setVisible(false);
            menuItemDebugOff.setEnabled(true).setVisible(true);
        } else {
            menuItemDebugOn.setEnabled(true).setVisible(true);
            menuItemDebugOff.setEnabled(false).setVisible(false);
        }
        if (snd) {
            menuItemSoundOn.setEnabled(false).setVisible(false);
            menuItemSoundOff.setEnabled(true).setVisible(true);
        } else {
            menuItemSoundOn.setEnabled(true).setVisible(true);
            menuItemSoundOff.setEnabled(false).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_settings:
                btnSettings(v);
                return true;
            case R.id.menu_exit:
                finish();
                System.exit(0);
            case R.id.menu_debug:
                VersionHelper.refreshActionBarMenu(this);
                dbg = true;
                return true;
            case R.id.menu_debug_off:
                VersionHelper.refreshActionBarMenu(this);
                dbg = false;
                return true;
            case R.id.menu_map_view:
                VersionHelper.refreshActionBarMenu(this);
                Intent intent = new Intent(getApplicationContext(), MapView.class);
                startActivity(intent);
                return true;
            case R.id.menu_privacy:
                VersionHelper.refreshActionBarMenu(this);
                showPrivacyDialog();
                return true;
            case R.id.menu_volume:
                VersionHelper.refreshActionBarMenu(this);
                snd = true;
                //Click Sound
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.soho);
                mp.start();
                return true;
            case R.id.menu_mute:
                VersionHelper.refreshActionBarMenu(this);
                snd = false;
                return true;

        }
        return super.onOptionsItemSelected(item);
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        deviceID = mTelephonyManager.getDeviceId();
        if (deviceID == null) {
            deviceID = "0000000000000000";
        }
        /*
         * getSubscriberId() returns the unique subscriber ID,
         * For example, the IMSI for a GSM phone.
         */
        subscriberID = mTelephonyManager.getSubscriberId();
        if (subscriberID == null) {
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

        deviceSerial = Build.SERIAL;
        if (deviceSerial.equals(null)) {
            deviceSerial = "0000000000000000";
        }

        U2ID = Installation.id(this);

        model = Build.MODEL;
        brand = Build.BRAND;
        product = Build.PRODUCT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            os = Build.VERSION.BASE_OS;
        }
        osId = Build.VERSION.RELEASE;

        setMCC(mTelephonyManager.getNetworkOperator().substring(0, 3));
        setMNC(mTelephonyManager.getNetworkOperator().substring(3));
        setOperatorName(mTelephonyManager.getNetworkOperatorName());

        Log.i(TAG, brand);
        Log.i(TAG, model);
        Log.i(TAG, product);
        Log.i(TAG, Build.ID);
        Log.i(TAG, operatorName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, Build.VERSION.BASE_OS);
        }
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
        uri = URL + "/myapp/api-token-auth/";
        // the request
        try {

            HttpPost httpPost = new HttpPost(new URI(uri));
            jsonT = "{\r\n    \"username\": \"test@hua.gr\",\r\n    \"password\": \"1234\"\r\n}";
            HttpEntity entity = new StringEntity(jsonT, "utf-8");
            httpPost.setEntity(entity);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
            RestTask task = new RestTask(this, ACTION_FOR_INTENT_CALLBACK);
            task.execute(httpPost);
            progress = ProgressDialog.show(this, "Authenticating ...", "Getting Token ...", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    //Show Privacy Policy Message
    private void showPrivacyDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Privacy Policy of V2of")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(myvar)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.apply();
    }


    @Override
    public void onClick(View view) {
        Intent i;

        final MediaPlayer mp = MediaPlayer.create(this, R.raw.soho);

        switch (view.getId()) {
            /**
             * Check if appropriate permissions have been granted.
             */
            case R.id.drivecardId:
                //Click Sound
                if (snd) {
                    mp.start();
                }
                getMeasurements();
                i = new Intent(this, MapsActivity.class);
                startActivity(i);
                break;
            case R.id.logincardId:
                //Click Sound
                if (snd) {
                    mp.start();
                }
                i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
            case R.id.chartscardId:
                //Click Sound
                if (snd) {
                    mp.start();
                }
                i = new Intent(this, ChartsActivity.class);
                startActivity(i);
                break;
            case R.id.settingscardId:
                //Click Sound
                if (snd) {
                    mp.start();
                }
                btnSettings(view);
                break;
            case R.id.privacycardId:
                //Click Sound
                if (snd) {
                    mp.start();
                }
                showPrivacyDialog();
                break;
            default:
                break;
        }
        Log.d(TAG, new Object() {
        }.getClass().getEnclosingMethod().getName());

    }
}