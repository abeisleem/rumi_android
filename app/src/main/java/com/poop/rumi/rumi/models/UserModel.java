package com.poop.rumi.rumi.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by dita on 4/13/18.
 */

public class UserModel implements Parcelable{

    public String id;
    public String name;
    public String username;
    public String email;
    public boolean isRegistered;
    public ArrayList<String> roommates;

    public UserModel(String name) {
        this.id = "";
        this.name = name;
        this.username = "";
        this.email = "";
        this.isRegistered = false;
        this.roommates = null;
    }

    public UserModel(JSONObject json) {
        try {
            this.name = json.getString("name");
            this.email = json.getString("email");
            this.isRegistered = json.getBoolean("isRegistered");
            this.id = json.getString("_id");

            if(this.isRegistered) {
                this.username = json.getString("username");

                String [] temp = convertToStringArr(json, "roommates");
                this.roommates = (ArrayList<String>) Arrays.asList(temp);
                }
        } catch(Exception ex) {
            Log.e("USER-MODEL", "Cannot parse user data");
        }
    }


    private String[] convertToStringArr(JSONObject json, String key) throws JSONException {
        JSONArray arr = json.getJSONArray(key);
        int len = arr.length();
        String[] res = new String[len];

        for(int i = 0; i < len; i++) {
            res[i] = arr.getString(i);
        }

        return res;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * Parcelable Methods
     */

    private UserModel(Parcel in) {
        id = in.readString();
        name = in.readString();
        username = in.readString();
        email = in.readString();
        isRegistered = in.readByte() != 0;
        roommates = in.createStringArrayList();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeByte((byte) (isRegistered ? 1 : 0));
        dest.writeStringList(roommates);
    }
}
