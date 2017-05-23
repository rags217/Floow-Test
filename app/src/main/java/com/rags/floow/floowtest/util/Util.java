package com.rags.floow.floowtest.util;

import android.content.SharedPreferences;

import com.rags.floow.floowtest.FloowContract;

/**
 * Created by development on 14/05/2017.
 */

public class Util {

    //public static String UPDATE_LAT_KEY = "UPDATE_LAT_KEY";
    //public static String UPDATE_LON_KEY = "UPDATE_LON_KEY";
    //public static String UPDATE_TIME_KEY = "UPDATE_TIME_KEY";
    public static String HISTORY_MAP_ACTIVITY_EXTRA_TRIP_ID = "HISTORY_MAP_ACTIVITY_EXTRA_TRIP_ID";

    public static String GPS_TRACKER_STATE = "GPS_TRACKER_STATE";
    public static String TRIP_ID_KEY = "TRIP_ID_KEY";

    public static String WHERE_TRIP_ID_EQUALS = FloowContract.LocationEntry.COLUMN_NAME_TRIP_ID +" = ?";

    public static void setGPSTrackerState(SharedPreferences pref, boolean setOn) {
        pref.edit().putBoolean(GPS_TRACKER_STATE, setOn).commit();
    }

    public static boolean getGPSTrackerState(SharedPreferences pref) {
        return pref.getBoolean(GPS_TRACKER_STATE, false);
    }

    public static int getTripID(SharedPreferences pref) {
        return pref.getInt(TRIP_ID_KEY, 0);
    }

    public static void incrementTripID(SharedPreferences pref) {
        pref.edit().putInt(TRIP_ID_KEY, getTripID(pref)+1).commit();
    }
}
