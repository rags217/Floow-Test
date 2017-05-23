package com.rags.floow.floowtest;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.*;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.rags.floow.floowtest.util.Util;

import java.util.ArrayList;
import java.util.Date;

//Class that carries out location tracking
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    GoogleApiClient mGoogleAPiClient;
    LocationRequest mLocationRequest;
    FloowDbHelper helper;
    SQLiteDatabase db;

    int ONE_SECOND_IN_MS = 1000;
    int INTERVAL_IN_SECONDS = 10;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //Listen when the location request interval changes
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MainActivity.ACTION_CHANGE_INTERVAL) && mGoogleAPiClient != null) {
                if(Build.VERSION.SDK_INT <= 22 || (Build.VERSION.SDK_INT > 22 && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPiClient, LocationService.this);

                    mLocationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(intent.getIntExtra(MainActivity.INTERVAL_KEY, 60) * ONE_SECOND_IN_MS)
                            .setFastestInterval(intent.getIntExtra(MainActivity.INTERVAL_KEY, 60) * ONE_SECOND_IN_MS);

                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPiClient, mLocationRequest, LocationService.this);
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        helper = new FloowDbHelper(this);
        db = helper.getWritableDatabase();

        mGoogleAPiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleAPiClient.connect();

        registerReceiver(receiver, new IntentFilter(MainActivity.ACTION_CHANGE_INTERVAL));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleAPiClient.disconnect();
        unregisterReceiver(receiver);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println(">>onConnectionFailed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(Build.VERSION.SDK_INT <= 22 || (Build.VERSION.SDK_INT > 22 && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)))
        {
            //Insert location to database
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(INTERVAL_IN_SECONDS * ONE_SECOND_IN_MS);
            mLocationRequest.setFastestInterval(INTERVAL_IN_SECONDS * ONE_SECOND_IN_MS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println(">>onConnectionSuspended");
    }

    //Called when new location avaialble
    @Override
    public void onLocationChanged(Location location) {
        android.util.Log.i("LocationService","onLocationChanged");
        if(location == null)
            return;
        MainActivity.requiresUpdate = true;
        ContentValues locationEntry = new ContentValues();
        locationEntry.put(FloowContract.LocationEntry.COLUMN_NAME_TRIP_ID, Util.getTripID(PreferenceManager.getDefaultSharedPreferences(this)));
        locationEntry.put(FloowContract.LocationEntry.COLUMN_NAME_LATITUDE, location.getLatitude());
        locationEntry.put(FloowContract.LocationEntry.COLUMN_NAME_LONGITUDE, location.getLongitude());
        locationEntry.put(FloowContract.LocationEntry.COLUMN_NAME_TIME_STAMP, String.valueOf(DateFormat.format("dd/MM HH:mm", new Date())));
        long l = db.insert(FloowContract.LocationEntry.TABLE_NAME, null, locationEntry);
        android.util.Log.i("LocationService","Row inserted ID:::"+l);
        //if(MainActivity.points == null)
        //    MainActivity.points = new ArrayList<LatLng>();
        //MainActivity.points.add(new LatLng(location.getLatitude(), location.getLongitude()));
        Intent updateLocationIntent = new Intent(MainActivity.ACTION_UPDATE);
        //updateLocationIntent.putExtra(Util.UPDATE_LAT_KEY, location.getLatitude());
        //updateLocationIntent.putExtra(Util.UPDATE_LON_KEY, location.getLongitude());
        //updateLocationIntent.putExtra(Util.UPDATE_TIME_KEY, String.valueOf(DateFormat.format("HH:mm", new Date())));
        sendBroadcast(updateLocationIntent);



    }
}
