package com.poop.rumi.rumi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.poop.rumi.rumi.fragments.SearchRoommateFragment;
import com.poop.rumi.rumi.fragments.UserSettingsFragment;
import com.poop.rumi.rumi.models.UserModel;
import com.poop.rumi.rumi.ocr.OcrCaptureActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoommateActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String roommateListUrl;
    private String currUserToken;
    private String currUser;
//    private JSONObject responseJSON;
    private RequestQueue requestQueue;
    private JSONArray roommateList;

    private ListView contentRoommateListView;
    private List<UserModel> roommateArrayList = new ArrayList<>();
    private RoommateRecycleViewAdapter roommateListAdapter;
    private RecyclerView roommateRecyclerView;
    private boolean success = false;

    private SearchRoommateFragment searchRoommateFragment;
    private UserSettingsFragment userSettingsFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roommate);

        requestQueue = Volley.newRequestQueue(this);
        roommateListUrl = getString(R.string.base_url) + getString(R.string.roommate_list_url);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.user_preferences_key), Context.MODE_PRIVATE);
        String currSessionToken = preferences.getString(getString(R.string.current_user_token), "");
        String currSessionUserJson = preferences.getString(getString(R.string.current_user_json_to_string), "");

        // If the user isn't logged in, take them to the login page
        if(currSessionToken.isEmpty() || currSessionUserJson.isEmpty()) {
            startActivity(new Intent(RoommateActivity.this, LoginActivity.class));
            finish();

            return;
        }

        currUser = currSessionUserJson;
        currUserToken = currSessionToken;

        View contentRoommateView = findViewById(R.id.content_roommate_layout);

        roommateRecyclerView = contentRoommateView.findViewById(R.id.roommate_recycler_view);
        roommateRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        roommateListAdapter = new RoommateRecycleViewAdapter(this, roommateArrayList, currUserToken);

        // Set list of roommates to the view in UI
        roommateRecyclerView.setAdapter(roommateListAdapter);

        (new RoommateTask()).execute((Void) null);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        JSONObject currUserJSON = null;

        try {
            currUserJSON = new JSONObject(currUser);
        } catch(Exception ex) {
            Log.i("DASHBOARD", "Error creating JSON");
        }

        // Set user info in the sidebar
        TextView sidebarName = header.findViewById(R.id.sidebar_name);
        TextView sidebarEmail = header.findViewById(R.id.sidebar_email);

        try {
            sidebarName.setText(currUserJSON.getString("name"));
            sidebarEmail.setText(currUserJSON.getString("email"));
        } catch(Exception ex) {
            Log.i("DASHBOARD", "Error setting sidebar info for user.");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Roommates");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_roommate_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchRoommateFragment = new SearchRoommateFragment();
                Bundle args = new Bundle();
                args.putString("user", currUser);
                args.putString("token", currUserToken);
                searchRoommateFragment.setArguments(args);

                searchRoommateFragment.show(getFragmentManager(), "Search Roommate Fragment");
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            userSettingsFragment = new UserSettingsFragment();
            Bundle args = new Bundle();
            args.putString("user", currUser);
            args.putString("token", currUserToken);
            userSettingsFragment.setArguments(args);

            userSettingsFragment.show(getFragmentManager(), "Add Roommate Fragment");
        }
        if(id == R.id.action_logout){
            // Take the user's token out
            SharedPreferences preferences = getSharedPreferences(getString(R.string.user_preferences_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.current_user_token), "");
            editor.apply();

            Intent logoutIntent = new Intent(RoommateActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            MessagePopups.showToast(this, "Logged out!");
            startActivity(logoutIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scan)
        {
            Intent i = new Intent(this, DashboardActivity.class);
            i.putExtra(getString(R.string.current_user_json_to_string), currUser);
            i.putExtra(getString(R.string.current_user_token), currUserToken);

            startActivity(i);
            onPause();
        }
        else if (id == R.id.nav_roommates)
        {
            Intent getRoommateActivity = new Intent(getApplicationContext(), RoommateActivity.class);

            // Put the user info JSON in the intent to pass it to the next activity
            getRoommateActivity.putExtra(getString(R.string.current_user_json_to_string), currUser);
            getRoommateActivity.putExtra(getString(R.string.current_user_token), currUserToken);

            startActivity(getRoommateActivity);
        }
        else if (id == R.id.nav_transactions)
        {
            Intent getTransactionActivity = new Intent(getApplicationContext(), TransactionActivity.class);
            getTransactionActivity.putExtra(getString(R.string.current_user_token), currUserToken);
            getTransactionActivity.putExtra(getString(R.string.current_user_json_to_string), currUser);
            startActivity(getTransactionActivity);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void makeListRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, roommateListUrl,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Success callback
                        Log.i("ROOMMATE", response.toString());
//                        responseJSON = response;
                        try {
                            roommateList = response.getJSONArray("roommates");
                            roommateArrayList.clear();

                            int numRoommates = roommateList.length();

                            for(int i = 0; i < numRoommates; i++) {
                                try {
                                    JSONObject currJSON = roommateList.getJSONObject(i);
                                    UserModel userModel = new UserModel(currJSON);
                                    roommateArrayList.add(userModel);
                                    roommateListAdapter.notifyDataSetChanged();

                                } catch (Exception ex) {
                                    Log.e("ROOMMATES", "Cannot get roommates");
                                }
                            }

                            success = true;

                        } catch(Exception ex) {
                            Log.e("ROOMMATE", "Error parsing JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error callback
                        Log.i("ROOMMATE", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Content-Type", "application/json");
                headers.put("Authorization", currUserToken);

                return headers;
            }
        };

        requestQueue.add(request);
    }

    public class RoommateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
//            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            makeListRequest();

            try {
                Thread.sleep(2000);

            } catch(Exception ex) {
                Log.e("DASHBOARD", "Error fetching dashboard: " + ex.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
//            progressBar.setVisibility(View.GONE);

            if (success) {
                roommateListAdapter.notifyDataSetChanged();
            } else {
                roommateListAdapter.notifyDataSetChanged();
                Toast.makeText(RoommateActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

