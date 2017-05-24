package com.rags.floow.floowtest;


//Data model class for passing value to Jouney list
public class History {

    String title;
    int tripID;

    public History(String title, int tripID) {
        //Title to be shown in the list item. start_time - end_time
        this.title = title;

        //Trip id of the journey to enable delete and retreive the selected journey
        this.tripID = tripID;
    }

    public String getTitle() {
        return title;
    }

    public int getTripID() {
        return tripID;
    }

}

