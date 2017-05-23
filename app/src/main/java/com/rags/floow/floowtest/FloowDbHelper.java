package com.rags.floow.floowtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



// Manages Database handler
public class FloowDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "floowdb.db";


    public FloowDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Called when the database handler is created.
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FloowContract.SQL_CREATE_LOCATION_ENTRY_TABLE);
    }

    //Called when the databse upgrades to next version
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FloowContract.SQL_DELETE_LOCATION_ENTRY_TABLE);
        onCreate(db);
    }

    //Called when the databse downgrades to previous version
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
