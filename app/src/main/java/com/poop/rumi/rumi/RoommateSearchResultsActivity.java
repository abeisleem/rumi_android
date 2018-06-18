package com.poop.rumi.rumi;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.poop.rumi.rumi.models.UserModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoommateSearchResultsActivity extends AppCompatActivity {

    private List<UserModel> searchResultsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RoommateSearchResultsRecycleViewAdapter adapter;

    private String searchQuery;

    private String currUserToken;
    private RequestQueue requestQueue;

    private EditText staticRoommateName;
    private EditText staticRoommateEmail;
    private Button staticRoommateAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roommate_search_results);

//        searchQuery = findViewById(R.id.search_roommate);
        currUserToken = getIntent().getStringExtra("token");
        searchQuery = getIntent().getStringExtra("searchQuery");

        requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(this);
        recyclerView = findViewById(R.id.roommate_search_results_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoommateSearchResultsRecycleViewAdapter(this, searchResultsList, currUserToken);
        recyclerView.setAdapter(adapter);

        searchUsers();

        View staticRoommateForm = findViewById(R.id.add_static_roommate_form_view);
        staticRoommateName = staticRoommateForm.findViewById(R.id.static_roommate_name);
        staticRoommateEmail = staticRoommateForm.findViewById(R.id.email_add_static_roommate_form);
        staticRoommateAddButton = staticRoommateForm.findViewById(R.id.add_static_roommate_button);

        staticRoommateAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String staticName = staticRoommateName.getText().toString();
                String staticEmail = staticRoommateEmail.getText().toString();
                boolean success = true;

                if(staticName.isEmpty()) {
                    staticRoommateName.setError("Please provide a name.");
                    success = false;
                }

                if(staticEmail.isEmpty()) {
                    staticRoommateEmail.setError("Please provide an email.");
                    success = false;
                }

                if(success) {
                    // Handle static roommate addition
                    addStaticRoommate(staticName, staticEmail);
                }
            }
        });
    }

    private void searchUsers() {
        String findRoommatesUrl = getString(R.string.base_url) + getString(R.string.find_users_url);

        JSONObject params = new JSONObject();
        try {
            params.put("roommate_name", searchQuery);
        } catch(Exception ex) {
            Log.e("ROOMMATE-SEARCH-RESULTS", "Error forming JSON request body");
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, findRoommatesUrl, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Success callback
                        Log.i("ROOMMATE-SEARCH-RESULTS", response.toString());

                        try {
                            boolean success = response.getBoolean("success");

                            if(success) {
                                JSONArray responseArr = response.getJSONArray("users");
                                int len = responseArr.length();

                                for(int i = 0; i < len; i++)
                                    searchResultsList.add(new UserModel(responseArr.getJSONObject(i)));

                                adapter.notifyDataSetChanged();
                            }
                            else {
                                MessagePopups.showToast(getApplicationContext(), "Error fetching search results.");
                            }

                        } catch(Exception ex) {
                            Log.e("ADD-ROOMMATE", "Failed parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error callback
                        Log.e("ADD-ROOMMATE", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", currUserToken);

                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void addStaticRoommate(final String staticName, String staticEmail) {
        String addStaticUrl = getString(R.string.base_url) + getString(R.string.roommate_add_static_url);

        JSONObject params = new JSONObject();
        JSONObject staticRoommate = new JSONObject();

        try {
            staticRoommate.put("name", staticName);
            staticRoommate.put("email", staticEmail);
            staticRoommate.put("isRegistered", false);
            params.put("roommate", staticRoommate);
        } catch(Exception ex) {
            Log.e("ROOMMATE-ADD-STATIC", "Error forming JSON request body");
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, addStaticUrl, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Success callback
                        Log.i("ROOMMATE-ADD-STATIC", response.toString());

                        try {
                            boolean success = response.getBoolean("success");

                            if(success) {
                                MessagePopups.showToast(getApplicationContext(), "Added " + staticName + "!");
                                staticRoommateName.clearComposingText();
                                staticRoommateEmail.clearComposingText();

                            }
                            else {
                                MessagePopups.showToast(getApplicationContext(), "Error fetching search results.");
                            }

                        } catch(Exception ex) {
                            Log.e("ADD-ROOMMATE", "Failed parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error callback
                        Log.e("ADD-ROOMMATE", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", currUserToken);

                return headers;
            }
        };

        requestQueue.add(request);
    }
}
