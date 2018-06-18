package com.poop.rumi.rumi;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.poop.rumi.rumi.models.DashboardContentModel;
import com.poop.rumi.rumi.models.ReceiptModel;
import com.poop.rumi.rumi.models.RoommateModel;
import com.poop.rumi.rumi.models.TransactionModel;

import java.util.List;

public class DashboardRecycleViewAdapter extends RecyclerView.Adapter<DashboardRecycleViewAdapter.CustomViewHolder> {
    private List<DashboardContentModel> dashboardList;
    private Context mContext;

    public DashboardRecycleViewAdapter(Context context, List<DashboardContentModel> dashboardList) {
        this.dashboardList = dashboardList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final DashboardContentModel currItem = dashboardList.get(i);

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
        return (null != dashboardList ? dashboardList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView title;
        protected TextView subtitle;
        protected TextView desc;
        protected TextView desc2;

        public CustomViewHolder(View view) {
            super(view);
            this.image = view.findViewById(R.id.image);
            this.title = (TextView) view.findViewById(R.id.title);
            this.subtitle = (TextView) view.findViewById(R.id.subtitle);
            this.desc = (TextView) view.findViewById(R.id.desc);
            this.desc2 = (TextView) view.findViewById(R.id.desc2);
            view.findViewById(R.id.button).setVisibility(View.GONE);
        }
    }
}