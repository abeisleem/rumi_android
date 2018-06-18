package com.poop.rumi.rumi.models;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by dita on 4/11/18.
 */

public class RoommateModel extends DashboardContentModel {
    public String firstName;
    public String lastName;
    public String preferredName;
    public String address;
    public String email;
    public String homePhone;
    public String cellPhone;

    private JSONObject json;

    public RoommateModel(String firstName, String lastName, String preferredName, String address, String email, String homePhone, String cellPhone, String date) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.preferredName = preferredName;
        this.address = address;
        this.email = email;
        this.homePhone = homePhone;
        this.cellPhone = cellPhone;
        convertDate(date);
    }

    public RoommateModel(JSONObject json) {
        this.json = json;
        convertJson();
    }

    private void convertJson() {
        try {
            this.firstName = json.getString("firstName");
            this.lastName = json.getString("lastName");
            this.preferredName = json.getString("preferredName");
            this.address = json.getString("address");
            this.email = json.getString("email");
            this.homePhone = json.getString("homePhone");
            this.cellPhone = json.getString("cellPhone");
            convertDate(json.getString("date_unix"));
        } catch(Exception ex) {
            Log.e("ROOMMATE-MODEL", "Error parsing JSON");
        }
    }
}
