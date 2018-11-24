package gr.hua.www.v2of2.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import gr.hua.www.v2of2.BaseActivity;

/**
 * Created by father on 3/8/17.
 */

public class StickyService extends Service {

    private final String TAG = getClass().getName(); // logging purposes

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "App just got removed from Recents!");
    }
}