package com.example.mazzdev.spotifystreamer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import com.example.mazzdev.spotifystreamer.services.MusicService;

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
}
