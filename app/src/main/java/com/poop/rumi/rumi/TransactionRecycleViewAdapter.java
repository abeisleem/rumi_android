package com.poop.rumi.rumi;

/**
 * Created by dita on 4/21/18.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Line;
import com.poop.rumi.rumi.models.TransactionModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class TransactionRecycleViewAdapter extends RecyclerView.Adapter<TransactionRecycleViewAdapter.CustomViewHolder> {
    private List<TransactionModel> transactionList;
    private Context mContext;

    public TransactionRecycleViewAdapter(Context context, List<TransactionModel> dashboardList) {
        this.transactionList = dashboardList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_list_card_view, null);
        final CustomViewHolder viewHolder = new CustomViewHolder(view);

        // On click expand
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewHolder.cardHelperMessage.getVisibility() == View.GONE) {
                    viewHolder.cardHelperMessage.setVisibility(View.VISIBLE);
                    viewHolder.listView.setVisibility(View.GONE);
                    viewHolder.splitPriceTitle.setVisibility(View.GONE);
                    viewHolder.ogPriceTitle.setVisibility(View.GONE);
                }
                else {
                    viewHolder.cardHelperMessage.setVisibility(View.GONE);
                    viewHolder.listView.setVisibility(View.VISIBLE);
                    viewHolder.splitPriceTitle.setVisibility(View.VISIBLE);
                    viewHolder.ogPriceTitle.setVisibility(View.VISIBLE);
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final TransactionModel currItem = transactionList.get(i);

        customViewHolder.storeName.setText(currItem.storeName);
        customViewHolder.date.setText(currItem.billDate);
        customViewHolder.billCode.setText(currItem.billCode);
        customViewHolder.participants.setText(formatStringArray(currItem.transactionNames));

        ArrayList<TransactionModel.TransactionItemModel> transactionItemModelList = currItem.transactionList;
        TransactionItemAdapter roommateAdapter = new TransactionItemAdapter(mContext, transactionItemModelList);
        customViewHolder.listView.setAdapter(roommateAdapter);

        ListAdapter listAdapter = customViewHolder.listView.getAdapter();

        if(listAdapter == null)
            return;

        int totalHeight = 0;
        for (int j = 0; j < listAdapter.getCount(); j++) {
            View listItem = listAdapter.getView(j, null, customViewHolder.listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = customViewHolder.listView.getLayoutParams();
        params.height = totalHeight + (customViewHolder.listView.getDividerHeight() * (listAdapter.getCount() - 1));
        customViewHolder.listView.setLayoutParams(params);
        customViewHolder.listView.requestLayout();
    }

    private String formatStringArray(String[] arr) {
        StringBuilder sb = new StringBuilder();
        int len = arr.length;

        for(int i = 0; i < len; i++) {
            sb.append(arr[i]);

            if(i < (len - 1))
                sb.append(", ");
        }

        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return (null != transactionList ? transactionList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView storeName;
        protected TextView date;
        protected TextView billCode;
        protected TextView participants;
        protected ListView listView;
        protected TextView cardHelperMessage;
        protected TextView splitPriceTitle;
        protected TextView ogPriceTitle;

        public CustomViewHolder(View view) {
            super(view);

            this.storeName = view.findViewById(R.id.store_name);
            this.date = view.findViewById(R.id.date);
            this.billCode = view.findViewById(R.id.bill_code);
            this.participants = view.findViewById(R.id.participants);
            this.cardHelperMessage = view.findViewById(R.id.card_helper_message);
            this.listView = view.findViewById(R.id.roommates_list_view);
            this.splitPriceTitle = view.findViewById(R.id.split_price_title);
            this.ogPriceTitle = view.findViewById(R.id.og_price_title);
            listView.setVisibility(View.GONE);
            splitPriceTitle.setVisibility(View.GONE);
            ogPriceTitle.setVisibility(View.GONE);
        }
    }

    public class TransactionItemAdapter extends ArrayAdapter<TransactionModel.TransactionItemModel> {

        public TransactionItemAdapter(Context context, ArrayList<TransactionModel.TransactionItemModel> models) {
            super(context, 0, models);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TransactionModel.TransactionItemModel currModel = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.transaction_list_item_card_view, parent, false);
            }

            TextView roommateName = convertView.findViewById(R.id.roommate_name);
            roommateName.setText(currModel.name);

            TransactionItemListAdapter itemsListAdapter = new TransactionItemListAdapter(convertView.getContext(), currModel.itemsList);
            ListView itemsListView = convertView.findViewById(R.id.items_list_view);
            itemsListView.setAdapter(itemsListAdapter);

            ListAdapter listAdapter = itemsListView.getAdapter();

            if(listAdapter != null) {
            int totalHeight = 0;
            for (int j = 0; j < listAdapter.getCount(); j++) {
                View listItem = listAdapter.getView(j, null, itemsListView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = itemsListView.getLayoutParams();
            params.height = totalHeight + (itemsListView.getDividerHeight() * (listAdapter.getCount() - 1));
            itemsListView.setLayoutParams(params);
            itemsListView.requestLayout();
            }

            return convertView;
        }

    }

    public class TransactionItemListAdapter extends ArrayAdapter<TransactionModel.TransactionItemListModel> {

        private ArrayList<TransactionModel.TransactionItemListModel> list;

        public TransactionItemListAdapter(Context context, ArrayList<TransactionModel.TransactionItemListModel> models) {
            super(context, 0, models);
            list = models;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TransactionModel.TransactionItemListModel currModel = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.transaction_item_list_view, parent, false);
            }

            TextView itemName = convertView.findViewById(R.id.item_name);
            TextView ogPrice = convertView.findViewById(R.id.item_og_price);
            TextView splitPrice = convertView.findViewById(R.id.item_split_price);

            // Bold the total, which is the last 'item' in the list
            if(list.size() - 1 == position) {
                itemName.setTypeface(itemName.getTypeface(), Typeface.BOLD);
                ogPrice.setTypeface(ogPrice.getTypeface(), Typeface.BOLD);
                splitPrice.setTypeface(splitPrice.getTypeface(), Typeface.BOLD);
            } else {
                itemName.setTypeface(itemName.getTypeface(), Typeface.NORMAL);
                ogPrice.setTypeface(ogPrice.getTypeface(), Typeface.NORMAL);
                splitPrice.setTypeface(splitPrice.getTypeface(), Typeface.NORMAL);
            }

            itemName.setText(currModel.item);
            ogPrice.setText("$" + currModel.ogPrice);
            splitPrice.setText("$" + currModel.splitPrice);

            return convertView;
        }

    }
}
