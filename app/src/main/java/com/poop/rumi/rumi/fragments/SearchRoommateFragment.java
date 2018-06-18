package com.poop.rumi.rumi.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.poop.rumi.rumi.R;
import com.poop.rumi.rumi.RoommateActivity;
import com.poop.rumi.rumi.RoommateSearchResultsActivity;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by dita on 4/9/18.
 */

public class SearchRoommateFragment extends DialogFragment {

    private EditText searchQuery;

    private View dialogView;

    private JSONObject currUserJSON;
    private String currUserToken;
    private RequestQueue requestQueue;
    private AlertDialog alertDialog;

    public static int SEARCH_REQUEST_CODE = 420;
//    private static int ADDED_ROOMMATE_CODE = 6969;

//    HashMap<String, String> newRoommateMap;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.fragment_add_roommate, null);

        String currUser = getArguments().getString("user");
        currUserToken = getArguments().getString("token");
        requestQueue = Volley.newRequestQueue(getActivity());

        try {
            currUserJSON = new JSONObject(currUser);
        } catch(Exception ex) {
            Log.e("ADD-ROOMMATE", "Cannot create user JSON");
        }

        searchQuery = dialogView.findViewById(R.id.search_roommate);

        builder.setPositiveButton(R.string.search_users, null);
        builder.setTitle("Search Users");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((RoommateActivity)getActivity()).makeListRequest();
                        SearchRoommateFragment.this.getDialog().cancel();
                    }
                });

        alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.WHITE);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String query = searchQuery.getText().toString();

                        if(query.isEmpty())
                            searchQuery.setError("Input in a valid search");
                        else {
                            Intent getSearchResults = new Intent(SearchRoommateFragment.this.getActivity(), RoommateSearchResultsActivity.class);

                            getSearchResults.putExtra("searchQuery", query);
                            getSearchResults.putExtra("token", currUserToken);

                            startActivityForResult(getSearchResults, SEARCH_REQUEST_CODE);
                        }
                    }
                });
            }
        });

        return alertDialog;
    }

}
