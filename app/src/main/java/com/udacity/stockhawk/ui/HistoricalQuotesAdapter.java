package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class HistoricalQuotesAdapter extends RecyclerView.Adapter<HistoricalQuotesAdapter.VideoViewHolder> {

    //final private ListItemClickListener mOnClickListener;

    private int mNumberItems;

    final private ArrayList<Pair<String,String>> history;

    /*public interface ListItemClickListener {
        void onClick(int clickedItemIndex);
    }*/

    public HistoricalQuotesAdapter(ArrayList<Pair<String,String>> history/*, ListItemClickListener listener*/) {
        Timber.d("Create Adapter");
        mNumberItems = history.size();
        //mOnClickListener = listener;
        this.history = history;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_history, viewGroup, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    class VideoViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView tvDate;
        TextView tvPrice;

        public VideoViewHolder(View itemView) {
            super(itemView);

            tvDate = (TextView) itemView.findViewById(R.id.list_item_history_date);
            tvPrice = (TextView) itemView.findViewById(R.id.list_item_history_price);
            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            tvDate.setText(history.get(listIndex).first);
            tvPrice.setText(history.get(listIndex).second);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            //mOnClickListener.onClick(clickedPosition);
        }
    }
}
