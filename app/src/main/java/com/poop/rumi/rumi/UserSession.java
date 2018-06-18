package com.poop.rumi.rumi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dita on 4/8/18.
 */

public class UserSession {

    private SharedPreferences preferences;

    public UserSession(Context context) {
        preferences = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
    }

    public void setUserToken(Context context, String userToken) {
        preferences = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.current_user_token), userToken);
        editor.commit();
    }

    public String getUserToken(Context context) {
        preferences = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.current_user_token), "");
    }


}
