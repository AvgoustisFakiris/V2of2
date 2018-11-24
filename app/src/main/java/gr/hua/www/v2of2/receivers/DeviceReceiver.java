package gr.hua.www.v2of2.receivers;

/**
 * Created by father on 9/9/15.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import gr.hua.www.v2of2.BaseActivity;
import gr.hua.www.v2of2.MessageActivity;


public class DeviceReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getName(); // logging purposes

    @Override
    public void onReceive(Context mContext, Intent mIntent) {
        // Network receiver
        final ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.i(TAG, "isConnected=" + String.valueOf(isConnected));
        if (isConnected) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // if wifi is connected
                if (activeNetwork != null) {
                    BaseActivity.wifiEnabled = isConnected;
                }
                Log.i(TAG, "wifiEnabled=" + String.valueOf(BaseActivity.wifiEnabled));
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // if connected to the mobile provider's data plan
                if (activeNetwork != null) {
                    BaseActivity.mobileEnabled = isConnected;
                }
                Log.i(TAG, "mobileEnabled=" + String.valueOf(BaseActivity.mobileEnabled));
            }
        } else {
            // not connected to the internet
            Log.i(TAG, "isConnected=" + String.valueOf(isConnected));
            Intent i = new Intent(mContext, MessageActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("Alert", "internet");
            mContext.startActivity(i);
        }


        // Location receiver. This includes three kind of providers.
        LocationManager lManager;
        lManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        /*getting status of the passive provider*/
        BaseActivity.passiveEnabled = lManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        if (BaseActivity.passiveEnabled) {
            BaseActivity.passiveEnabled = true;
            Log.i(TAG, "passiveEnabled=" + String.valueOf(BaseActivity.passiveEnabled));
        } else {
            BaseActivity.passiveEnabled = false;
            Log.i(TAG, "passiveEnabled=" + String.valueOf(BaseActivity.passiveEnabled));
        }

        /*getting status of the gps provider*/
        BaseActivity.gpsEnabled = lManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.i(TAG, mIntent.getAction().toString());
        if (BaseActivity.gpsEnabled) {
            BaseActivity.gpsEnabled = true;
            Log.i(TAG, "gpsEnabled=" + String.valueOf(BaseActivity.gpsEnabled));
        } else {
            BaseActivity.gpsEnabled = false;
            Log.i(TAG, "gpsEnabled=" + String.valueOf(BaseActivity.gpsEnabled));
        }

        /*getting status of network provider*/
        BaseActivity.networkEnabled = lManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (BaseActivity.networkEnabled) {
            Log.i(TAG, "networkEnabled=" + String.valueOf(BaseActivity.networkEnabled));
        } else {
            Log.i(TAG, "networkEnabled=" + String.valueOf(BaseActivity.networkEnabled));
        }

//        if (!BaseActivity.gpsEnabled && !BaseActivity.networkEnabled && !BaseActivity.passiveEnabled) {
        if (!BaseActivity.gpsEnabled && !BaseActivity.networkEnabled) {
            Intent i = new Intent(mContext, MessageActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("Alert", "location");
            mContext.startActivity(i);
        }

    }

}
