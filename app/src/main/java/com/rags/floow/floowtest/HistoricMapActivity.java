package com.rags.floow.floowtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rags.floow.floowtest.util.Util;

import java.util.ArrayList;
import java.util.List;

public class HistoricMapActivity extends AppCompatActivity implements OnMapReadyCallback, ViewTreeObserver.OnGlobalLayoutListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    int tripID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tripID = getIntent().getIntExtra(Util.HISTORY_MAP_ACTIVITY_EXTRA_TRIP_ID, 0);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapFragment.getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        mapFragment.getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);

        if(tripID > 0)
            new LoadJourneyTask().execute();
    }

    private class LoadJourneyTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            SQLiteDatabase db = new FloowDbHelper(HistoricMapActivity.this).getWritableDatabase();
            String[] projection = {FloowContract.LocationEntry.COLUMN_NAME_LATITUDE, FloowContract.LocationEntry.COLUMN_NAME_LONGITUDE, FloowContract.LocationEntry.COLUMN_NAME_TIME_STAMP};
            return db.query(FloowContract.LocationEntry.TABLE_NAME, projection, FloowContract.LocationEntry.COLUMN_NAME_TRIP_ID + "=?", new String[] {String.valueOf(tripID)}, null, null, null);
        }

        protected void onPostExecute(Cursor result) {
            if (result != null && result.getCount() >0) {

                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                result.moveToFirst();
                for (int i=0; i< result.getCount(); i++) {
                    result.moveToPosition(i);
                    System.out.println("TIME:::"+result.getString(result.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_TIME_STAMP)));
                    double latitude = result.getDouble(result.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_LATITUDE));
                    double longitude = result.getDouble(result.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_LONGITUDE));
                    LatLng point = new LatLng(latitude, longitude);
                    options.add(point);
                    builder.include(point);

                    if(i == 0) {
                        LatLng start = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(start).title("start").icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location)));
                    }

                    if(i == result.getCount()-1) {
                        LatLng end = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(end).title("end").icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location)));
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
