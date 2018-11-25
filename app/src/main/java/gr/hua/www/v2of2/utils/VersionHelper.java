package gr.hua.www.v2of2.utils;

/**
 * Created by Αυγουστής on 16/10/2018.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;

public class VersionHelper {
    public static void refreshActionBarMenu(Activity activity)
    {
        if (Build.VERSION.SDK_INT >= 11)
            activity.invalidateOptionsMenu();
    }
}
