package com.poop.rumi.rumi;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.poop.rumi.rumi.models.UserModel;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoommateRecycleViewAdapter extends RecyclerView.Adapter<RoommateRecycleViewAdapter.CustomViewHolder> {
    private List<UserModel> roommateList;
    private Context mContext;
    private String currUserToken;
    private RequestQueue requestQueue;

    public RoommateRecycleViewAdapter(Context context, List<UserModel> roommateList, String token) {
        this.roommateList = roommateList;
        this.mContext = context;
        this.currUserToken = token;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        requestQueue = Volley.newRequestQueue(mContext);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final UserModel currRoommate = roommateList.get(i);

        customViewHolder.title.setText(currRoommate.name);
        customViewHolder.subtitle.setText(currRoommate.email);

        if(currRoommate.isRegistered) {
            customViewHolder.desc.setVisibility(View.VISIBLE);
            customViewHolder.desc.setText("@" + currRoommate.username);
        }
        else {
            customViewHolder.desc.setVisibility(View.GONE);
        }

        customViewHolder.deleteRoommate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteRoommate(currRoommate.name, currRoommate.id);
            }
        });
    }

    public void deleteRoommate(final String deleteName, String deleteRoommate) {
        String deleteRoommateUrl = mContext.getString(R.string.base_url) + mContext.getString(R.string.roommate_delete_url);
        deleteRoommateUrl += "/" + deleteRoommate;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, deleteRoommateUrl,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Success callback
                        Log.i("ROOMMATE", response.toString());
//                        responseJSON = response;
                        try {
                            boolean success = response.getBoolean("success");

                            if(success) {
                                MessagePopups.showToast(mContext, "Deleted " + deleteName);
                                ((RoommateActivity)mContext).makeListRequest();

                            } else {
                                MessagePopups.showToast(mContext, "Failed to delete roommate");
                            }
                        } catch(Exception ex) {
                            Log.e("ROOMMATE", "Error parsing JSON: " + ex.getMessage());
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

    @Override
    public int getItemCount() {
        return (null != roommateList ? roommateList.size() : 0);
    }

    // Inherited from Dashboard, roommates list only uses title, subtitle
    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView title;
        protected TextView subtitle;
        protected TextView desc;
        protected Button deleteRoommate;

        public CustomViewHolder(View view) {
            super(view);
            this.image = view.findViewById(R.id.image);
            this.image.setVisibility(View.GONE);
            this.title = (TextView) view.findViewById(R.id.title);
            this.subtitle = (TextView) view.findViewById(R.id.subtitle);
            this.desc = (TextView) view.findViewById(R.id.desc);
            this.deleteRoommate =  view.findViewById(R.id.button);

            deleteRoommate.setTextColor(Color.BLACK);
            deleteRoommate.setText("Delete Roommate");
            deleteRoommate.setBackgroundColor(Color.WHITE);
        }
    }
}