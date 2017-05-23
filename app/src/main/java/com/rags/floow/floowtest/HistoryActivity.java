package com.rags.floow.floowtest;

import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.rags.floow.floowtest.util.Util;

import java.util.ArrayList;


public class HistoryActivity extends AppCompatActivity implements OnItemClickListener {

    ListView historyListview;
    TextView emptyList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListview = (ListView)findViewById(R.id.history_listview);
        emptyList = (TextView)findViewById(R.id.empty_list);

        new GetHistoryTask().execute();
     }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        History history = (History)parent.getAdapter().getItem(position);
        Intent historyMapIntent = new Intent(this, HistoricMapActivity.class);
        historyMapIntent.putExtra(Util.HISTORY_MAP_ACTIVITY_EXTRA_TRIP_ID, history.tripID);
        startActivity(historyMapIntent);
    }


    private class GetHistoryTask extends AsyncTask<Void, Void, ArrayList<History>> {

        @Override
        protected ArrayList<History> doInBackground(Void... params) {
            ArrayList<History> histories = new ArrayList<History>();

            SQLiteDatabase db = new FloowDbHelper(HistoryActivity.this).getWritableDatabase();
            Cursor start = db.rawQuery("SELECT * FROM location_entry WHERE _id IN(SELECT DISTINCT MIN(_id) FROM location_entry GROUP BY entry_trip_id)", null);
            Cursor end = db.rawQuery("SELECT * FROM location_entry WHERE _id IN(SELECT DISTINCT MAX(_id) FROM location_entry GROUP BY entry_trip_id)", null);

            int count = start.getCount();
            if(count == 0)
                return null;
            for(int i=0; i< count; i++) {
                start.moveToPosition(i);
                end.moveToPosition(i);
                String fromTo = start.getString(start.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_TIME_STAMP)) + " - " + end.getString(end.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_TIME_STAMP));
                histories.add(new History(fromTo, start.getInt(start.getColumnIndex(FloowContract.LocationEntry.COLUMN_NAME_TRIP_ID))));
            }


            return histories;
        }

        protected void onPostExecute(ArrayList<History> histories) {
            if(histories != null) {
                historyListview.setVisibility(View.VISIBLE);
                emptyList.setVisibility(View.GONE);

                HistoryAdapter adapter = new HistoryAdapter(HistoryActivity.this, histories);
                historyListview.setAdapter(adapter);
                historyListview.setOnItemClickListener(HistoryActivity.this);
            } else {
                historyListview.setVisibility(View.GONE);
                emptyList.setVisibility(View.VISIBLE);
            }
        }
    }
}
