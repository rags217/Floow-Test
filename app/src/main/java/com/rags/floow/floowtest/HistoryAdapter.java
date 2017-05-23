package com.rags.floow.floowtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;


public class HistoryAdapter extends ArrayAdapter {

    Context mContext;
    ArrayList<History> mHistories;

    public HistoryAdapter(Context context, ArrayList<History> histories) {
        super(context, 0, histories);
        this.mContext = context;
        this.mHistories = histories;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //Get the current history item
        History history = (History)getItem(position);
        final int tripID = history.getTripID();

        //create view if does not exist
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_list_item, parent, false);

        TextView tvName = (TextView) convertView.findViewById(R.id.title);
        tvName.setText(history.getTitle());


        ImageButton delete = (ImageButton) convertView.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, R.string.confirm_action, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_delete, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mHistories.remove(position);
                                notifyDataSetChanged();
                                new DeleteJourneyTask(tripID, position).execute();
                            }
                        })
                        .show();
            }
        });

        return convertView;
    }

    //Async task in background thread when deleting a journey
    private class DeleteJourneyTask extends AsyncTask<Void, Void, Integer> {

        int tripID, position;
        DeleteJourneyTask(int tripID, int position) {
            this.tripID = tripID;
            this.position = position;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            SQLiteDatabase db = new FloowDbHelper(mContext).getWritableDatabase();
            int l = db.delete(FloowContract.LocationEntry.TABLE_NAME, FloowContract.LocationEntry.COLUMN_NAME_TRIP_ID + "=" + tripID, null);
            return l;
        }

        protected void onPostExecute(long l) {
        }
    }
}
