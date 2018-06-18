package com.poop.rumi.rumi.models;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by dita on 4/12/18.
 */

public class DashboardContentModel {

    Date date;
    private SimpleDateFormat format;

    DashboardContentModel() {
        this.format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getDefault());

        this.date = null;
    }

//    public int compareTo(Object o) {
//        DashboardContentModel d = (DashboardContentModel)o;
//
//        return d.date.compareTo(this.date);
//    }

    public Date getDate() {
        return this.date;
    }

    void convertDate(String s) {
        try {
            this.date = format.parse(s);
        } catch(Exception ex) {
            Log.e("DASHBOARD-CONTENT-MODEL", "Error parsing date: " + ex.getMessage());
        }
    }
}
