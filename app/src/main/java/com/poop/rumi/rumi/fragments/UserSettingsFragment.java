package com.poop.rumi.rumi.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.poop.rumi.rumi.MessagePopups;
import com.poop.rumi.rumi.R;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dita on 4/10/18.
 */

public class UserSettingsFragment extends DialogFragment {

    private View dialogView;
    private TextView mName;
    private TextView mUsername;
    private TextView mEmail;
    private EditText mNewPassword;
    private Button mChangePasswordButton;
    private Button mNewPasswordButton;

    private String currUserToken;
    private RequestQueue requestQueue;

    private String changePasswordURL;
    private JSONObject currUserJSON;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.fragment_user_settings, null);

        changePasswordURL = getString(R.string.base_url) + getString(R.string.change_password_url);

        String currUser = getArguments().getString("user");
        currUserToken = getArguments().getString("token");
        requestQueue = Volley.newRequestQueue(getActivity());

        try {
            currUserJSON = new JSONObject(currUser);
        } catch(Exception ex) {
            Log.e("USER-SETTINGS", "Error getting user information.");
        }

        mName = dialogView.findViewById(R.id.user_name);
        mUsername = dialogView.findViewById(R.id.user_username);
        mEmail = dialogView.findViewById(R.id.user_email);
        mNewPassword = dialogView.findViewById(R.id.user_new_password);
        mNewPasswordButton = dialogView.findViewById(R.id.new_password_button);
        mChangePasswordButton = dialogView.findViewById(R.id.change_password_button);

        mNewPassword.setVisibility(View.GONE);
        mNewPasswordButton.setVisibility(View.GONE);
        mChangePasswordButton.setVisibility(View.VISIBLE);

        try {
            mName.setText(currUserJSON.getString("name"));
            mUsername.setText(currUserJSON.getString("username"));
            mEmail.setText(currUserJSON.getString("email"));
        } catch(Exception ex) {
            Log.e("USER-SETTINGS", "Error getting user information.");
        }

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNewPasswordButton.setVisibility(View.VISIBLE);
                mNewPassword.setVisibility(View.VISIBLE);
                mChangePasswordButton.setVisibility(View.GONE);
            }
        });

        mNewPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mNewPassword.getText().toString().isEmpty())
                    mNewPassword.setError("Password cannot be empty.");
                else
                    changePassword(mNewPassword.getText().toString());
            }
        });

        builder.setView(dialogView);
        builder.setTitle("User Settings");
        builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UserSettingsFragment.this.getDialog().cancel();
            }
        });

        final AlertDialog currDialog = builder.create();
        currDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                currDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
            }
        });

        return currDialog;
    }

    private void changePassword(String newPassword) {
        final JSONObject requestJSON = new JSONObject();

        try {
            requestJSON.put("_id", currUserJSON.getString("id"));
            requestJSON.put("name", currUserJSON.getString("name"));
            requestJSON.put("username", currUserJSON.getString("username"));
            requestJSON.put("email", currUserJSON.getString("email"));
            requestJSON.put("password", newPassword);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, changePasswordURL, requestJSON,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Response
                            boolean success = false;

                            try {
                                success = response.getBoolean("success");

                            } catch(Exception ex) {
                                Log.e("USER-SETTINGS", "Error parsing response.");
                            }

                            if(success) {
                                MessagePopups.showToast(UserSettingsFragment.this.getContext(), "Updated password!");
                                mNewPassword.setVisibility(View.GONE);
                                mNewPasswordButton.setVisibility(View.GONE);
                                mChangePasswordButton.setVisibility(View.VISIBLE);
                            }
                            else {
                                MessagePopups.showToast(UserSettingsFragment.this.getContext(), "Failed updating password");
                            }
                        }
                    },
                    new Response.ErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("USER-SETTINGS", "Error resetting password");
                            MessagePopups.showToast(UserSettingsFragment.this.getContext(), "Failed updating password");
                        }
                    }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", currUserToken);

                    return headers;
                }

                @Override
                public byte[] getBody() {

                    try {
                        Log.i("JSON", requestJSON.toString());
                        return requestJSON.toString().getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            requestQueue.add(request);

        } catch(Exception ex) {
            Log.e("USER-SETTINGS", "Error setting new password");
        }
    }

}
