package com.poop.rumi.rumi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.poop.rumi.rumi.fragments.SearchRoommateFragment;
import com.poop.rumi.rumi.models.UserModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dita on 4/17/18.
 */

public class RoommateSearchResultsRecycleViewAdapter extends RecyclerView.Adapter<RoommateSearchResultsRecycleViewAdapter.CustomViewHolder>{
    private List<UserModel> searchResultList;
    private Context mContext;
    private String token;
    private RequestQueue requestQueue;

    public RoommateSearchResultsRecycleViewAdapter(Context context, List<UserModel> roommateList, String currUserToken) {
        this.searchResultList = roommateList;
        this.mContext = context;
        this.token = currUserToken;
        requestQueue = Volley.newRequestQueue(mContext);
    }

    @Override
    public RoommateSearchResultsRecycleViewAdapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.roommate_search_result_card_view, null);
        RoommateSearchResultsRecycleViewAdapter.CustomViewHolder viewHolder = new RoommateSearchResultsRecycleViewAdapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RoommateSearchResultsRecycleViewAdapter.CustomViewHolder customViewHolder, int i) {
        final UserModel currRoommate = searchResultList.get(i);

        customViewHolder.name.setText(currRoommate.name);
        customViewHolder.username.setText(("@" + currRoommate.username));

        customViewHolder.addRoommateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRoommate(currRoommate.name, currRoommate.id);

            }
        });
    }

    private void addRoommate(final String roommateName, String roommateId) {
        String addRoommateUrl = mContext.getString(R.string.base_url) + mContext.getString(R.string.roommate_add_url);

        JSONObject params = new JSONObject();
        try {
            params.put("_id", roommateId);
        } catch(Exception ex) {
            Log.e("ROOMMATE-SEARCH-RESULTS", "Error forming JSON request body");
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, addRoommateUrl, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Success callback
                        Log.i("ROOMMATE-SEARCH-RESULTS", response.toString());

                        try {
                            boolean success = response.getBoolean("success");

                            if(success) {
                                MessagePopups.showToast(mContext, "Added " + roommateName + "!");
                                ((RoommateActivity)mContext).makeListRequest();
                            }
                            else {
                                MessagePopups.showToast(mContext, "Error fetching search results.");
                            }

                        } catch(Exception ex) {
                            Log.e("ROOMMATE-SEARCH-RESULTS", "Failed parsing JSON response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Error callback
                        Log.e("ROOMMATE-SEARCH-RESULTS", error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);

                return headers;
            }
        };

        requestQueue.add(request);
    }

    @Override
    public int getItemCount() {
        return (null != searchResultList ? searchResultList.size() : 0);
    }

    // Inherited from Dashboard, roommates list only uses title, subtitle
    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView username;
        protected Button addRoommateButton;

        public CustomViewHolder(View view) {
            super(view);

            this.name = view.findViewById(R.id.roommate_result_name);
            this.username = view.findViewById(R.id.roommate_result_username);
            this.addRoommateButton = view.findViewById(R.id.roommate_add_button);
        }
    }
}
