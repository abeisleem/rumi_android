package com.poop.rumi.rumi;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by dita on 4/10/18.
 */

public class MessagePopups {

    public static void showToast(Context context, String message) {
        Toast currToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        currToast.setGravity(Gravity.CENTER, 0 , 0);
        currToast.show();
    }
}
