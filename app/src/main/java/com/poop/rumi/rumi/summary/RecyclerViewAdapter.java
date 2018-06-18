package com.poop.rumi.rumi.summary;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.poop.rumi.rumi.R;
import com.poop.rumi.rumi.transaction.TransactionListAdapter;

import java.io.PipedOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> mImageNames;
    private ArrayList<String> mImagesURL;
    private Context mContext;
    private ViewGroup parentViewGroup;

    private ViewHolder lastViewHolder;
    private int lastNamePos;
    private String lastNameTapped;

    // SummaryListAdapter DATA for purposes of regenerating list when new name is tapped.
    private SummaryListAdapter summaryListAdapter;
    private ArrayList<ParticipantInfo> participantList;
    private ListView listViewItems;


    private final int primaryColor = Color.rgb(87, 188, 150);
    private final int secondaryColor = Color.rgb(238, 238, 255);

    public RecyclerViewAdapter(Context mContext, ArrayList<String> mImageNames, ArrayList<String> mImagesURL) {
        this.mImageNames = mImageNames;
        this.mImagesURL = mImagesURL;
        this.mContext = mContext;

        lastNamePos = -1;
        lastViewHolder = null;



        Log.d("TESTERSS", "INITING RECYLCER");

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called Viewholder!");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_name_box, parent, false);

        parentViewGroup = parent;


        Log.d("TESTERSS", "CREATING RECYLCER");
        return new ViewHolder(view);

    }


    public void passSummaryData(SummaryListAdapter summaryListAdapter, ArrayList<ParticipantInfo> participantList, ListView listViewItems) {


        Log.d("TESTERSS", "PASSING SUMMARY DATA");

        this.summaryListAdapter = summaryListAdapter;
        this.participantList = participantList;
        this.listViewItems = listViewItems;

    }



    public void highLightFirstName() {


        parentViewGroup.getChildAt(0).setBackgroundColor(primaryColor);
        lastNamePos = 0;


        Log.d("TESTERSS", "COLORING FIRST CHILD");
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if(lastNamePos == -1) {

            Log.d("TESTERSS", "SETTING CHILD @ " + position);
            holder.parentLayout.findViewById(R.id.name_layout).setBackgroundColor(primaryColor);

            lastNameTapped = mImageNames.get(position);
            lastViewHolder = holder;
            lastNamePos = position;
        }

        Glide.with(mContext)
                .asBitmap()
                .load(mImagesURL.get(position))
                .into(holder.image);

        holder.name.setText(mImageNames.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set to primary color to indicate which participant is selected
                holder.parentLayout.findViewById(R.id.name_layout).setBackgroundColor(primaryColor);

                // Set previously selected participant to secondary color and remove all associated highlights
                if(lastViewHolder != null && holder != lastViewHolder) {
                    lastViewHolder.parentLayout.findViewById(R.id.name_layout).setBackgroundColor(secondaryColor);

                }


                /**
                 * Regenerate list array adapter based on newly tapped name
                 */
                summaryListAdapter = new SummaryListAdapter(mContext,
                        R.layout.layout_participation_view,
                        participantList.get(position).getTriadList());

                listViewItems.setAdapter(summaryListAdapter);


                lastNameTapped = mImageNames.get(position);
                lastViewHolder = holder;
                lastNamePos = position;


            }
        });




    }

    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.textView_image_name);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    public int getLastNamePos(){
        return lastNamePos;
    }

    public String getLastNameTapped(){
        return lastNameTapped;
    }



}
