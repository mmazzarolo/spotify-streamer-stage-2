package com.example.mazzdev.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Matteo on 10/06/2015.
 */
public class Utility {

    public static String getPreferredCountry(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_country_key);
        String def = context.getString(R.string.pref_country_default);
        return prefs.getString(key, def);
    }

    public static boolean getPreferredLockScreenNotifInfo(Context context)  {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_notif_lock_screen_key);
        return prefs.getBoolean(key, true);
    }

    public static void sendBroadcast(Context context, String string) {
        Intent intent = new Intent(string);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
