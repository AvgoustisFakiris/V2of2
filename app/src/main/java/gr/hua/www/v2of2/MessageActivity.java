package gr.hua.www.v2of2;

/**
 * Created by father on 9/9/15.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class MessageActivity extends Activity {

    private final String TAG = getClass().getName(); // logging purposes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ........................");

        // get Intent that started this Activity
        Intent i = getIntent();
        String alert = i.getStringExtra("Alert");
        if (alert == null) {
            Toast.makeText(getApplicationContext(), "Unknown Alert Message!", Toast.LENGTH_LONG).show();
            finish();
        }

        switch (alert) {
            case "wifi":
                WiFiErrorMessage();
                break;
            case "location":
                LocationServicesErrorMessage();
                break;
            case "internet":
                internetErrorMessage();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Unknown Alert Message!", Toast.LENGTH_LONG).show();
                finish();
        }

    }


    protected void internetErrorMessage() {

        AlertDialog ad = new AlertDialog.Builder(this)
                .setMessage("You need internet connection for this app! " +
                        "Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {


                            public void onClick(DialogInterface dialog, int id) {

                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                                MessageActivity.this.finish();

                            }


                        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        MessageActivity.this.finish();

                    }

                }).create();
        ad.setCancelable(true);
        ad.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                MessageActivity.this.finish();
            }

        });
        ad.show();
    }


    private void WiFiErrorMessage() {

        AlertDialog ad = new AlertDialog.Builder(this)
                .setMessage("Internet Access is required!")
                .setTitle("Unable to connect")
                .setPositiveButton("Please enable Wi-Fi.",
                        new DialogInterface.OnClickListener() {


                            public void onClick(DialogInterface dialog, int id) {

//                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
                                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                wifi.setWifiEnabled(true);//Turn on Wifi
                                MessageActivity.this.finish();

                            }


                        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        MessageActivity.this.finish();

                    }

                }).create();
        ad.setCancelable(true);
        ad.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                MessageActivity.this.finish();
            }

        });
        ad.show();
    }


    private void LocationServicesErrorMessage() {

        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle("Unable to get location")
                .setMessage("Location Services is required!")
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {

                                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(i);
                                MessageActivity.this.finish();

                            }


                        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        MessageActivity.this.finish();

                    }

                }).create();
        ad.setCancelable(true);
        ad.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                MessageActivity.this.finish();
            }

        });
        ad.show();
    }


    @SuppressWarnings("unused")
    private void ConnectionFailedErrorMessage() {

        AlertDialog ad = new AlertDialog.Builder(this).setMessage("V2of: Connection to Provider Failed!!").setPositiveButton("Go to V2of",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent();
                        i.setClassName("gr.hua.www.v2of2", "gr.hua.www.v2of2.MessageActivity");
                        i.setAction(Intent.ACTION_DEFAULT);
                        startActivity(i);
                        MessageActivity.this.finish();

                    }


                }).setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                MessageActivity.this.finish();
            }

        }).create();
        ad.setCancelable(false);
        ad.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                MessageActivity.this.finish();
            }

        });
        ad.show();
    }

}