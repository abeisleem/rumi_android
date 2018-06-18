package com.poop.rumi.rumi.models;

import android.util.Log;

import com.poop.rumi.rumi.Receipt;

import org.json.JSONObject;

/**
 * Created by dita on 4/11/18.
 */

public class ReceiptModel extends DashboardContentModel {
    public String key;
    public String name;
    public String link;

    private JSONObject json;

    public ReceiptModel(String key, String name, String link, String date) {
        this.key = key;
        this.name = name;
        this.link = link;

        convertDate(date);
    }

    public ReceiptModel(JSONObject json) {
        try {
            this.key = json.getString("key");
            this.name = json.getString("name");
            this.link = json.getString("link");
            convertDate(json.getString("date_unix"));
        } catch(Exception ex) {
            Log.e("RECEIPT-MODEL", "Error parsing JSON");
        }
    }
}
