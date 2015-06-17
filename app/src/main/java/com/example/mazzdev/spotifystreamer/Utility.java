package com.example.mazzdev.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Matteo on 10/06/2015.
 */
public class Utility {

    public static String getPreferredCountry(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_country_key),
                context.getString(R.string.pref_country_default));
    }

    public static void sendBroadcast(Context context, String string) {
        Intent onCompletedIntent = new Intent(string);
        LocalBroadcastManager.getInstance(context).sendBroadcast(onCompletedIntent);
    }
}
