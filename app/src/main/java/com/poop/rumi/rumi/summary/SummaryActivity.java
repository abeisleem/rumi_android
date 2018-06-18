package com.poop.rumi.rumi.summary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.poop.rumi.rumi.DashboardActivity;
import com.poop.rumi.rumi.MessagePopups;
import com.poop.rumi.rumi.R;
import com.poop.rumi.rumi.RoommateActivity;
import com.poop.rumi.rumi.models.UserModel;
import com.poop.rumi.rumi.transaction.Transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SummaryActivity extends AppCompatActivity {

    private final String TAG = "SummaryActivity";
    private final Context mContext = this;

    // Adapters
    private RecyclerViewAdapter nameListAdapter;
    private SummaryListAdapter summaryListAdapter;

    private String currUserID;

    //
    private ArrayList<Transaction> transactionList;
    private ArrayList<UserModel> addedNamesUM;
    private ArrayList<String> mNames;       // to pass to nameListAdapter
    private ArrayList<String> mIDs;
    private ArrayList<String> mImageUrls;

    //
    private ArrayList<ParticipantInfo> participantList;

    //
    private String currUserToken;
    private String currUser;

    private String receiptImagePath;
    private String storeName;
    private String dateOfCapture;

    private String billCode;

    private JSONObject transactionSchema;
    private RequestQueue requestQueue;
    private boolean success;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        requestQueue = Volley.newRequestQueue(this);

        Bundle transData = getIntent().getExtras();

        assert transData != null;

        currUserID = transData.getString("CURR_USER_ID");

        transactionList = transData.getParcelableArrayList("TRANSACTION");
        addedNamesUM = transData.getParcelableArrayList("PARTICIPANTS");
        mNames = transData.getStringArrayList("NAMES");

        mIDs = new ArrayList<>();
        for(UserModel u: addedNamesUM){
            mIDs.add(u.id);
        }


        currUser = transData.getString(getString(R.string.current_user_json_to_string));
        currUserToken = transData.getString(getString(R.string.current_user_token));

        storeName = transData.getString("STORE_NAME");
        receiptImagePath = transData.getString("RECEIPT_IMAGE_PATH");
        dateOfCapture = transData.getString("DATE_OF_CAPTURE");


        participantList = new ArrayList<>();

        tooDeepToGetIntoThis(); // .. to fill participantList []

        final ListView listViewItems = (ListView)findViewById(R.id.vertical_list_participation);

        initImageBitmaps();
        initRecyclerView();

        // take in the context, custom layout that made, arraylist(which is transactionList)
        summaryListAdapter = new SummaryListAdapter(this, R.layout.layout_participation_view, participantList.get(0).getTriadList());
        listViewItems.setAdapter(summaryListAdapter);

        nameListAdapter.passSummaryData(summaryListAdapter, participantList, listViewItems);


        generateSharedCode();


        Button nextButton = (Button)findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                generateJSONSchema();

                Intent intent = new Intent(getApplicationContext(),DashboardActivity.class);
                intent.putExtra(getString(R.string.current_user_json_to_string), currUser);
                intent.putExtra(getString(R.string.current_user_token), currUserToken);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        Button backButton = (Button)findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


    }


    private void tooDeepToGetIntoThis() {

        DecimalFormat df = new DecimalFormat("#.00");

        for (UserModel um : addedNamesUM)
            participantList.add(new ParticipantInfo(um.name));


        int numParticipants;
        Float eachPay;
        ArrayList<String> curNames;
        for (Transaction t : transactionList) {

            curNames = t.getNames();

            numParticipants = curNames.size();

            eachPay = t.getPrice() / numParticipants;
            df.format(eachPay);

            //Toast.makeText(this, "ITEM :" + t.getItem() + "<= " + curNames.toString(), Toast.LENGTH_LONG).show();

            for (String name : curNames)
                for (ParticipantInfo p : participantList)
                    if (p.getName().equals(name))
                        p.addItemPrice(t.getItem(), t.getPrice(), eachPay);

        }
    }


    private void initImageBitmaps(){

        mImageUrls = new ArrayList<>();

        for(int i = 0; i <  addedNamesUM.size(); i++)
            mImageUrls.add("");

    }

    private void initRecyclerView() {

        LinearLayoutManager layoutManager = new LinearLayoutManager( SummaryActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.horizontal_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        nameListAdapter = new RecyclerViewAdapter(this, mNames, mImageUrls);
        recyclerView.setAdapter(nameListAdapter);


    }

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 6) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();

    }

    public void generateSharedCode(){

        TextView textView = (TextView)findViewById(R.id.share_code);
        billCode = getSaltString();
        textView.setText(billCode);
    }


    private void generateJSONSchema() {

        JSONArray participantInfoSchema = new JSONArray();

        for (ParticipantInfo p : participantList) {

            JSONObject obj = new JSONObject();

            try {
                obj.put("name", p.getName());
                obj.put("items", p.getItems());
                obj.put("og_prices", p.getOgPrices());
                obj.put("split_prices", p.getOwedPrices());

                participantInfoSchema.put(obj);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        transactionSchema = new JSONObject();
        try {

            transactionSchema.put("bill_code", billCode);
            transactionSchema.put("roommates", new JSONArray(mIDs));
            transactionSchema.put("store_name", storeName);
            transactionSchema.put("receipt_link", "https://");
            transactionSchema.put("bill_date", dateOfCapture);
            transactionSchema.put("owner_id", currUserID);
            transactionSchema.put("transaction_list", participantInfoSchema);


        } catch (JSONException e) {
            e.printStackTrace();
        }

//        if(isConnected()){
//            makeTransactionPOST(transactionSchema);
////            (new TransactionTask()).execute((Void) null);
//
//        }
//        else
//        {
//            MessagePopups.showToast(mContext, "please connect to interwebs first");
//
//        }


        makeTransactionPOST(transactionSchema);
    }


    protected void makeTransactionPOST(final JSONObject obj) {

        final String transactionAddURL = getString(R.string.base_url) + getString(R.string.transaction_add);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, transactionAddURL, obj,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Success callback
                        Log.i(TAG, response.toString());
                        try {
                            success = response.getBoolean("success");

                            if(success) {
                                //MessagePopups.showToast(mContext, "Added " + obj.toString() + "!");
                                Log.d("TAG, SCHEMA", obj.toString());
                                //((SummaryActivity)mContext).makeListRequest();
                            }
                            else {
                                MessagePopups.showToast(mContext, "Error fetching search results.");
                            }


                        } catch(Exception ex) {
                            Log.e(TAG, "Error parsing JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error callback
                        Log.i(TAG, error.toString());
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





    @SuppressLint("StaticFieldLeak")
    private class TransactionTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            makeTransactionPOST(transactionSchema);

            try {
                Thread.sleep(2000);

            } catch(Exception ex) {
                Log.e(TAG, "Error fetching roommates: " + ex.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {

        }
    }



    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


//    private class SendDeviceDetails extends AsyncTask<String, Void, Void> {
//
//
//        @Override
//        protected void onPreExecute() {
//
//        }
//
//        @Override
//        protected Void doInBackground(String... params) {
//
//            final String transactionAddURL = getString(R.string.base_url) + getString(R.string.transaction_add);
//
//            String data = "";
//
//            HttpURLConnection httpURLConnection = null;
//            try {
//
//                httpURLConnection = (HttpURLConnection) new URL(transactionAddURL);
//                httpURLConnection.setRequestMethod("POST");
//
//                httpURLConnection.setDoOutput(true);
//
//                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
//                wr.writeBytes("PostData=" + params[1]);
//                wr.flush();
//                wr.close();
//
//                InputStream in = httpURLConnection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(in);
//
//                int inputStreamData = inputStreamReader.read();
//                while (inputStreamData != -1) {
//                    char current = (char) inputStreamData;
//                    inputStreamData = inputStreamReader.read();
//                    data += current;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (httpURLConnection != null) {
//                    httpURLConnection.disconnect();
//                }
//            }
//
//            return data;
//
//        }
//
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
//        }
//    }


}

