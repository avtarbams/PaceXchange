package com.example.paceexchange;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<InventoryData> mItemList;
    private View.OnClickListener mRowClickListener;
    private int mSelectedPosition = Adapter.NO_SELECTION;

    public RecyclerAdapter(Context context, ArrayList<InventoryData> listData, View.OnClickListener rowClickListener) {

        mContext = context;
        mItemList = listData;
        mRowClickListener = rowClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = View.inflate(mContext, R.layout.recycler_view, null);

        ViewHolder viewHolder = new ViewHolder(view, mRowClickListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        InventoryData inventoryDisplay = mItemList.get(position);
        ViewHolder vHolder = (ViewHolder) viewHolder;

        Picasso.get().load(inventoryDisplay.getUrl()).fit().centerCrop().into(vHolder.mItemImage);
        vHolder.mItemName.setText(inventoryDisplay.getTitle());
        vHolder.mItemType.setText(vHolder.itemView.getResources().getString(R.string.item_category_inventory_display, inventoryDisplay.getCategory()));
        vHolder.mItemTradeFor.setText(vHolder.itemView.getResources().getString(R.string.trade_item_requested_inventory_display, inventoryDisplay.getTradeInFor()));
        viewHolder.itemView.setOnClickListener(mRowClickListener);
        viewHolder.itemView.setTag(position);

    }


    @Override
    public int getItemCount() {

        return mItemList.size();
    }


    public InventoryData getItem(int position) {
        return mItemList.get(position);
    }

    public void removeInventoryItem(int position) {
        mItemList.remove(position);
    }


    private class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mItemImage;
        public TextView mItemName, mItemType, mItemTradeFor;

        public ViewHolder(@NonNull View itemView, View.OnClickListener rowClickListener) {
            super(itemView);

            this.itemView.setOnClickListener(rowClickListener);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mItemName = itemView.findViewById(R.id.itemName);
            mItemType = itemView.findViewById(R.id.itemType);
            mItemTradeFor = itemView.findViewById(R.id.itemTradeFor);

        }
    }
}
