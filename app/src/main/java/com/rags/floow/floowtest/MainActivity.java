package com.rags.floow.floowtest;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rags.floow.floowtest.util.Util;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ViewTreeObserver.OnGlobalLayoutListener {

    private GoogleMap mMap;
    public static String ACTION_UPDATE = "ACTION_UPDATE";
    public static String INTERVAL_KEY = "INTERVAL_KEY";
    public static String ACTION_CHANGE_INTERVAL = "ACTION_CHANGE_INTERVAL";

    final int INTERVAL_TEN_SEC = 10;
    final int INTERVAL_SIXTY_SEC = 60;

    FloatingActionButton trackerState;
    SharedPreferences pref;
    boolean isTrackerOn;
    Intent locationService;
    String label;
    SupportMapFragment mapFragment;

    boolean mapReady = false;

    public static Boolean requiresUpdate = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_UPDATE) && mapReady) {
                new HandleLocationUpdateTask().execute();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(Build.VERSION.SDK_INT > 22 && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED))
            trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
        else if(!isGPSEnabled && !isNetworkEnabled)
            trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorRed)));
        else if(Util.getGPSTrackerState(pref))
            trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
        else
            trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));

        if(requiresUpdate) {
            requiresUpdate = false;
            if(mapReady)
                new HandleLocationUpdateTask().execute();
        }
        Intent changeIntervalIntent = new Intent(ACTION_CHANGE_INTERVAL);
        changeIntervalIntent.putExtra(INTERVAL_KEY, INTERVAL_TEN_SEC);
        sendBroadcast(changeIntervalIntent);
        registerReceiver(receiver, new IntentFilter(ACTION_UPDATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        requiresUpdate = false;
        Intent changeIntervalIntent = new Intent(ACTION_CHANGE_INTERVAL);
        changeIntervalIntent.putExtra(INTERVAL_KEY, INTERVAL_SIXTY_SEC);
        sendBroadcast(changeIntervalIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT > 22 && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED))
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationService = new Intent(MainActivity.this,LocationService.class);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        trackerState = (FloatingActionButton) findViewById(R.id.trackerState);
        if(Util.getGPSTrackerState(pref)) {
            isTrackerOn = true;
            trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
            stopService(locationService);
            startService(locationService);
        } else {
            isTrackerOn = false;
            trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
        }


        trackerState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
                boolean isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(Build.VERSION.SDK_INT > 22 && (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)) {
                    Snackbar.make(view, "GRANT LOCATION PERMISSION", Snackbar.LENGTH_LONG)
                            .setAction("SETTINGS", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.rags.floow.floowtest")));
                                }
                            }).show();
                } else if(!isGPSEnabled && !isNetworkEnabled) {
                    Snackbar.make(view, "TURN ON LOCATION", Snackbar.LENGTH_LONG)
                            .setAction("SETTINGS", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }).show();
                } else if(isTrackerOn) {
                    Util.setGPSTrackerState(pref, false);
                    isTrackerOn = false;
                    stopService(locationService);
                    trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
                    Snackbar.make(view, "GPS tracker is OFF", Snackbar.LENGTH_LONG)
                            .setAction("Tracker", null).show();
                } else {
                    Util.setGPSTrackerState(pref, true);
                    isTrackerOn = true;
                    Util.incrementTripID(pref);
                    registerReceiver(receiver, new IntentFilter(ACTION_UPDATE));
                    startService(locationService);
                    trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
                    Snackbar.make(view, "GPS tracker is ON", Snackbar.LENGTH_LONG).setAction("Tracker", null);
                }



            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapReady = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapFragment.getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        mapFragment.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        mapReady = true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Boolean locationPermissionGranted = false;
        for(int i=0; i< grantResults.length; i++)
            if(grantResults[i] == 0)
                locationPermissionGranted = true;
        if(locationPermissionGranted) {
            if(Util.getGPSTrackerState(pref))
                trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGreen)));
            else
                trackerState.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
        } else
            Snackbar.make(findViewById(R.id.fab_coordinator_layout), R.string.abort_track, Snackbar.LENGTH_LONG).show();

    }

    private class HandleLocationUpdateTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            int tripID = Util.getTripID(pref);
            SQLiteDatabase db = new FloowDbHelper(MainActivity.this).getWritableDatabase();
            String[] projection = {FloowContract.LocationEntry.COLUMN_NAME_LATITUDE, FloowContract.LocationEntry.COLUMN_NAME_LONGITUDE, FloowContract.LocationEntry.COLUMN_NAME_TIME_STAMP};
            return db.query(FloowContract.LocationEntry.TABLE_NAME, projection, FloowContract.LocationEntry.COLUMN_NAME_TRIP_ID + "=?", new String[] {String.valueOf(tripID)}, null, null, null);
        }

        protected void onPostExecute(Cursor result) {
            if (result != null && result.getCount() >0) {
                result.moveToFirst();
                mMap.clear();
                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (int i=0; i< result.getCount(); i++) {
                    result.moveToPosition(i);
                    Log.i("MainActivity", "COUNT:::"+result.getCount());
                    double latitude = result.getDouble(result.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_LATITUDE));
                    double longitude = result.getDouble(result.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_LONGITUDE));
                    LatLng point = new LatLng(latitude, longitude);
                    options.add(point);
                    builder.include(point);
                    if(i == result.getCount()-1) {
                        LatLng currentLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title(label).icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location)));
                        label = result.getString(result.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_TIME_STAMP));
                    }
                }

                mMap.addPolyline(options);

                LatLngBounds bounds = builder.build();
                int padding = 10;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
            }
        }
    }

}
