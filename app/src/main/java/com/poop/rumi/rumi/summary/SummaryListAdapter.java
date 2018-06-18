package com.poop.rumi.rumi.summary;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.poop.rumi.rumi.R;

import java.util.ArrayList;


public class SummaryListAdapter extends ArrayAdapter<ParticipantInfo.ParticipantTriad> {

    private static final String TAG = "summaryListAdapter";
    private Context mContext;
    private int mResource;


    private ArrayList<ParticipantInfo.ParticipantTriad> participantInfo;



    private final int primaryColor = Color.rgb(87, 188, 150);
    private final int secondaryColor = Color.rgb(238, 238, 255);

    public SummaryListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ParticipantInfo.ParticipantTriad> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        participantInfo =  objects;
    }


    public static class ViewHolder{
        TextView itemTextView;
        TextView origPriceTextView;
        TextView owedPriceTextView;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View rowView = convertView;

        if(convertView == null){

            LayoutInflater inflater = LayoutInflater.from(mContext);
            rowView = inflater.inflate(mResource, parent, false);

            ViewHolder vh = new ViewHolder();
            vh.itemTextView = rowView.findViewById(R.id.item_name_view);
            vh.origPriceTextView = rowView.findViewById(R.id.original_price_view);
            vh.owedPriceTextView = rowView.findViewById(R.id.owed_price_view);

            rowView.setTag(vh);

        }

        final ViewHolder holder = (ViewHolder) rowView.getTag();


        // Get transaction information
        // Get transaction information
        final String item = getItem(position).getItem();
        final Float ogPrice = getItem(position).getOgPrice();
        final Float owedPrice = getItem(position).getOwedPrice();


        final LinearLayout linearLayout = (LinearLayout)rowView.findViewById(R.id.parent_layout_item_owed);


        linearLayout.isLongClickable();
        /**
        *   Adding/removing names to each item and highlighting appropriately
        * */
        final View finalRowView = rowView;

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        /**
         *   Long click listener to enable editing item name and price
         * */

        linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;

            }
        });


        // TODO: if there is only one participant: the owed price text view
        // TODO: will set the $Owed TextView same as the Original Price but with extra one decimal place
        // TODO: e.g.: $Price: $2.99 $Owed: 2.990

        // String.format("%.2f", owedPrice);

        holder.itemTextView.setText(item);
        holder.origPriceTextView.setText("$" + ogPrice.toString());
        holder.owedPriceTextView.setText("$" + String.format("%.3f", owedPrice).toString());

        return rowView;
    }



}