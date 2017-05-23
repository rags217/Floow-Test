package com.rags.floow.floowtest;

import android.provider.BaseColumns;

//This class consist of all variables to create Database
public final class FloowContract {

    /* Inner class that defines the table contents */
    public static class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location_entry";
        public static final String COLUMN_NAME_TRIP_ID = "entry_trip_id";
        public static final String COLUMN_NAME_LATITUDE = "entry_latitude";
        public static final String COLUMN_NAME_LONGITUDE = "entry_longitude";
        public static final String COLUMN_NAME_TIME_STAMP = "entry_time_stamp";
    }

    public static final String TEXT_TYPE = " TEXT";
    public static final String REAL_TYPE = " REAL";
    public static final String INTEGER_TYPE = "  INTEGER ";
    public static final String PRIMARY_KEY = "  PRIMARY KEY ";
    public static final String AUTOINCREMENT = "  AUTOINCREMENT ";
    public static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_LOCATION_ENTRY_TABLE =
            "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                    LocationEntry._ID + INTEGER_TYPE + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_TRIP_ID + INTEGER_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_LONGITUDE + TEXT_TYPE + COMMA_SEP +
                    LocationEntry.COLUMN_NAME_TIME_STAMP + TEXT_TYPE + " )";



    public static final String SQL_DELETE_LOCATION_ENTRY_TABLE = "DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;
}

