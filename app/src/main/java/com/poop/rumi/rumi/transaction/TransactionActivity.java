package com.poop.rumi.rumi.transaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import com.poop.rumi.rumi.R;
import com.poop.rumi.rumi.Receipt;
import com.poop.rumi.rumi.models.UserModel;
import com.poop.rumi.rumi.summary.SummaryActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "TransactionActivity";
    private final Context mContext = this;

    private RecyclerViewAdapter nameListAdapter;
    private TransactionListAdapter transListAdapter;

    private ArrayList<Transaction> transactionList;
    private ArrayList<UserModel> addedNamesUM;
    private ArrayList<String> mNames = new ArrayList<>(); // to disallow duplicate names & display names in summary
    private ArrayList<String> mImageUrls = new ArrayList<>();

    TextView store_restaurant;

    private Receipt mReceipt;
    private String currUserToken;
    private String currUser;
    private UserModel currUserModel;

    public ArrayList<UserModel> currUserRoommatesUM = new ArrayList<>();
    private ArrayList<String> roommateNames;
    private ArrayList<String> roommateIDs;

    private RequestQueue requestQueue;
    private JSONArray roommateList;
    private boolean success = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);
        Log.d(TAG, "onCreate: Started onCreate!");


        mReceipt = (Receipt) getIntent().getSerializableExtra("RECEIPT");

        currUserToken = mReceipt.getCurrUserToken();
        currUser = mReceipt.getCurrUser();
        try {
            currUserModel = new UserModel(new JSONObject(currUser));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestQueue = Volley.newRequestQueue(this);

//        roommateNames = new ArrayList<>();

        populateRoommateUserModelList();


        // Add transactions to the arraylist: take Transactions objects
        transactionList = new ArrayList<>();

        ArrayList<String> inputItems = mReceipt.getItems();
        ArrayList<Float> inputPrices = mReceipt.getPrices();

        int maxLength = Math.max(mReceipt.getItems().size(), mReceipt.getPrices().size());

        String tempStr;
        Float tempFlt;
        for(int i = 0; i < maxLength; i++)
        {

            if(i >= inputPrices.size()) {
                tempStr = inputItems.get(i);
                tempFlt = 0f;
            }
            else if(i >= inputItems.size()){
                tempStr = "";
                tempFlt = inputPrices.get(i);
            }
            else {
                tempStr = inputItems.get(i);
                tempFlt = inputPrices.get(i);
            }

            transactionList.add(new Transaction(tempStr,tempFlt));
        }



        // Adding current user to nameListAdapter
        addedNamesUM = new ArrayList<>();
        mImageUrls.add("");
        addedNamesUM.add(currUserModel);
        mNames.add(currUserModel.name);
//        roommateIDs.add(currUserModel.id);


        //initImageBitmaps();
        initRecyclerView();

        final ListView listViewItems = (ListView)findViewById(R.id.vertical_list_item_price_name);

        // take in the context, custom layout that made, arraylist(which is transactionList)
        transListAdapter = new TransactionListAdapter(this, R.layout.layout_transaction_view, transactionList);
        listViewItems.setAdapter(transListAdapter);

        // setting up co-dependencies
        // for sake of highlighting items based on most recently tapped name
        transListAdapter.setRecyclerViewAdapter(nameListAdapter);
        nameListAdapter.setTransactionListAdapter(transListAdapter);


        store_restaurant = findViewById(R.id.store_restaurant);
        store_restaurant.setText(mReceipt.getStoreName());
        store_restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openEditStoreNameDialog();} });

        Button nextButton = (Button)findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSummaryActivity();
            }
        });

        Button backButton = (Button)findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Button addButton = (Button)findViewById(R.id.button_add_person);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPersonDialog();
            }
        });

        // Add Item/Price Button
        Button add_item_price = (Button)findViewById(R.id.button_add_more_item_price);
        add_item_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddItemPriceDialog();

            }
        });

    }

    public void openAddPersonDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(TransactionActivity.this);

        LayoutInflater inflater = LayoutInflater.from(TransactionActivity.this);

        final View dialogView = inflater.inflate(R.layout.dialog_add_person,null);

        builder.setView(dialogView);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        // Finally, display the alert dialog
        dialog.show();

        final AutoCompleteTextView editText_get_names = (AutoCompleteTextView) dialogView.findViewById(R.id.editText_add_name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, roommateNames);

        editText_get_names.setThreshold(1);
        editText_get_names.setAdapter(adapter);


        Button keep_adding = (Button)dialogView.findViewById(R.id.button_keep_adding);
        keep_adding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String addedName = editText_get_names.getText().toString();
                String addedID = "";

                System.out.println("====== Keep adding button clicked!! ========== ");
                System.out.println(addedName);
                System.out.println("====== Keep adding button clicked!! ========== ");

                if(addedName.length() == 0) {
                    Toast.makeText(TransactionActivity.this, "Name field is empty.", Toast.LENGTH_SHORT).show();
                }else if(mNames.contains(addedName)) {
                    Toast.makeText(TransactionActivity.this, "Name already exists, please try a different name.", Toast.LENGTH_SHORT).show();
                }else{

                    UserModel userToAdd = null;
                    boolean addedFromDB = false;


                    for (int i = 0; i < roommateNames.size(); i++) {
                        if (roommateNames.get(i).equals(addedName)) {
                            addedID = roommateIDs.get(i);
                            break;
                        }
                    }

                    //Toast.makeText(TransactionActivity.this, "Tapped on item: " + addedName + "ID: " + addedID, Toast.LENGTH_SHORT).show();

                    for(UserModel u : currUserRoommatesUM) {
                        if (addedID.equals(u.id)) {
                            userToAdd = u;
                            addedFromDB = true;

                            Toast.makeText(TransactionActivity.this, "added from db", Toast.LENGTH_SHORT).show();

                            break;
                        }
                    }
                    
                    if(!addedFromDB){
                        userToAdd = new UserModel(addedName);
                        Toast.makeText(TransactionActivity.this, "added static user", Toast.LENGTH_SHORT).show();
                    }

                    mImageUrls.add("");
                    addedNamesUM.add(userToAdd);
                    mNames.add(userToAdd.name);

                    Toast.makeText(TransactionActivity.this, addedName + " added!", Toast.LENGTH_SHORT).show();

                    editText_get_names.setText(null);
                }

            }
        });

    }


    public void populateRoommateUserModelList (){

        (new RoommateTask()).execute((Void) null);

    }

    public void openAddItemPriceDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(TransactionActivity.this);

        LayoutInflater inflater = LayoutInflater.from(TransactionActivity.this);

        final View dialogView = inflater.inflate(R.layout.dialog_add_or_edit_item,null);

        builder.setView(dialogView);

        final EditText editText_item_name = (EditText) dialogView.findViewById(R.id.edit_item_name);
        final EditText editText_item_price = (EditText) dialogView.findViewById(R.id.edit_item_price);

        Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();

        // Set the positive button
        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                System.out.println("Item: **** "+editText_item_name.getText().toString());
//                System.out.println("Price: **** "+editText_item_price.getText().toString());

                Transaction t;
                try{

                    t = new Transaction(
                            editText_item_name.getText().toString(),
                            Float.parseFloat(editText_item_price.getText().toString()));
                }catch(Exception e){
                    Log.e("InvalidNumber","Can not parse empty float");
                        return; // Or another exception handling.
                }

                transactionList.add(t);

                dialog.dismiss();
            }
        });

        // Finally, display the alert dialog
        dialog.show();

    }

    public void openEditStoreNameDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(TransactionActivity.this);

        LayoutInflater inflater = LayoutInflater.from(TransactionActivity.this);

        final View dialogView = inflater.inflate(R.layout.dialog_edit_store_name,null);

        builder.setView(dialogView);

        final EditText edit_store_restaurant_name = (EditText)dialogView.findViewById(R.id.edit_store_name);
        Button btn_positive = (Button) dialogView.findViewById(R.id.dialog_positive_btn);

        // Create the alert dialog
        final AlertDialog dialog = builder.create();


        // Set positive/yes button click listener
        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                store_restaurant.setText(edit_store_restaurant_name.getText().toString());

                dialog.dismiss();
            }
        });

        // Finally, display the alert dialog
        dialog.show();

    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview.");

        LinearLayoutManager layoutManager = new LinearLayoutManager( TransactionActivity.this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.horizontal_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        nameListAdapter = new RecyclerViewAdapter(this, addedNamesUM, mImageUrls);
        recyclerView.setAdapter(nameListAdapter);

    }

    public void openSummaryActivity() {

        Intent intent = new Intent(this, SummaryActivity.class);

        intent.putExtra("TRANSACTION", transactionList);
        intent.putExtra("CURR_USER_ID", currUserModel.id);
        intent.putExtra("PARTICIPANTS", addedNamesUM);
        intent.putExtra("NAMES", mNames);

        intent.putExtra(getString(R.string.current_user_json_to_string), currUser);
        intent.putExtra(getString(R.string.current_user_token), currUserToken);

        intent.putExtra("STORE_NAME", store_restaurant.getText().toString());
        intent.putExtra("RECEIPT_IMAGE_PATH", mReceipt.getReceiptImagePath());
        intent.putExtra("DATE_OF_CAPTURE", mReceipt.getDateOfCapture());


        startActivity(intent);

    }



    protected void makeListRequest() {

        final String roommateListUrl = getString(R.string.base_url) + getString(R.string.roommate_list_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, roommateListUrl,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Success callback
                        Log.i("ROOMMATE", response.toString());
//                        responseJSON = response;
                        try {
                            roommateList = response.getJSONArray("roommates");

                            int numRoommates = roommateList.length();

                            for(int i = 0; i < numRoommates; i++) {
                                try {
                                    JSONObject currJSON = roommateList.getJSONObject(i);
                                    UserModel userModel = new UserModel(currJSON);
                                    currUserRoommatesUM.add(userModel);

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


    @SuppressLint("StaticFieldLeak")
    private class RoommateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            makeListRequest();

            try {
                Thread.sleep(2000);

            } catch(Exception ex) {
                Log.e(TAG, "Error fetching roommates: " + ex.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {

            roommateNames = new ArrayList<>();
            roommateIDs = new ArrayList<>();

            for(UserModel u : currUserRoommatesUM) {
                roommateNames.add(u.name);
                roommateIDs.add(u.id);
            }
//                Log.d("TESTIESS", roommateNames.toString());

        }
    }
//


}
