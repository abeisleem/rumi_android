package com.poop.rumi.rumi;

import android.annotation.TargetApi;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.poop.rumi.rumi.fragments.UserSettingsFragment;
import com.poop.rumi.rumi.models.DashboardContentModel;
import com.poop.rumi.rumi.models.ReceiptModel;
import com.poop.rumi.rumi.models.RoommateModel;
import com.poop.rumi.rumi.models.TransactionModel;
import com.poop.rumi.rumi.ocr.OcrCaptureActivity;
import com.poop.rumi.rumi.TransactionActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private Context mContext = this;

    private RecyclerView mRecyclerView;
    private TransactionRecycleViewAdapter adapter;
    private ProgressBar progressBar;
    private TextView sidebarName;
    private TextView sidebarEmail;

    // User values
    private UserSession userSession;
    private RequestQueue requestQueue;
    private String currUser;
    private String currUserToken;
    private JSONObject currUserJSON;
    private Boolean success = false;

    private ArrayList<TransactionModel> transactionsList;
    private ArrayList<RoommateModel> roommatesList;
    private ArrayList<ReceiptModel> receiptsList;
    private List<TransactionModel> dashboardList;

    private UserSettingsFragment userSettingsFragment;

    private int MAX_DASHBOARD_ITEMS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dashboardList = new ArrayList<>();
        transactionsList = new ArrayList<>();
        roommatesList = new ArrayList<>();
        receiptsList = new ArrayList<>();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.user_preferences_key), Context.MODE_PRIVATE);
        String currSessionToken = preferences.getString(getString(R.string.current_user_token), "");
        String currSessionUserJson = preferences.getString(getString(R.string.current_user_json_to_string), "");

        // If the user isn't logged in, take them to the login page
        if(currSessionToken.isEmpty() || currSessionUserJson.isEmpty()) {
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();

            return;
        }

        currUser = currSessionUserJson;
        currUserToken = currSessionToken;
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        try {
            currUserJSON = new JSONObject(currUser);
        } catch(Exception ex) {
            Log.i("DASHBOARD", "Error creating JSON");
        }

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);

        adapter = new TransactionRecycleViewAdapter(getApplicationContext(), dashboardList);
        mRecyclerView.setAdapter(adapter);

        // Get nested view from sidebar
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        // Set user info in the sidebar
        sidebarName = header.findViewById(R.id.sidebar_name);
        sidebarEmail = header.findViewById(R.id.sidebar_email);

        try {
            sidebarName.setText(currUserJSON.getString("name"));
            sidebarEmail.setText(currUserJSON.getString("email"));
        } catch(Exception ex) {
            Log.i("DASHBOARD", "Error setting sidebar info for user.");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Recent Transactions");
        setSupportActionBar(toolbar);

        new DashboardTask().execute((Void) null);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(view.getContext(), OcrCaptureActivity.class);
                i.putExtra(getString(R.string.current_user_json_to_string), currUser);
                i.putExtra(getString(R.string.current_user_token), currUserToken);

                startActivity(i);
                onPause();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

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

            Intent logoutIntent = new Intent(DashboardActivity.this, LoginActivity.class);
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

            Intent i = new Intent(mContext, DashboardActivity.class);
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

    private void getDashboard() {
        String dashboardUrl = getString(R.string.base_url) + getString(R.string.dashboard_url);

        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, dashboardUrl,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("DASHBOARD", response.toString());

                            try {
                                success = response.getBoolean("success");

                                if(success) {
                                    JSONArray arr = response.getJSONArray("transactions");
                                    int len = arr.length();

                                    for(int i = 0; i < len; i++) {
                                        dashboardList.add(new TransactionModel(arr.getJSONObject(i)));
                                        adapter.notifyDataSetChanged();
                                    }

                                    progressBar.setVisibility(View.GONE);
                                }
                            } catch(Exception ex) {
                                Log.e("DASHBOARD", "Failed parsing response: " + ex.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Failure Callback
                            success = false;
                            Log.e("DASHBOARD", "Failed to retrieve dashboard.");
                            MessagePopups.showToast(getApplicationContext(), "Failed to retrieve dashboard.");
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

        } catch(Exception ex) {
            success = false;
            Log.e("DASHBOARD", "Error parsing response.");
        }

    }

    public class DashboardTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            getDashboard();

            try {
                Thread.sleep(2000);

            } catch(Exception ex) {
                Log.e("DASHBOARD", "Error fetching dashboard: " + ex.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setVisibility(View.GONE);

            if (success) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyDataSetChanged();
                Toast.makeText(DashboardActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

