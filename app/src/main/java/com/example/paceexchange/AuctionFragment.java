package com.example.paceexchange;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AuctionFragment extends Fragment {

    private ImageView mItemImage;
    private TextView mItemName, mItemOwner;
    private String mImageURL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.auction_item_fragment, container, false);
        mItemImage = view.findViewById(R.id.itemImage);
        mItemName = view.findViewById(R.id.itemName);
        mItemOwner = view.findViewById(R.id.itemOwner);

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setURL(String url){
        mImageURL=url;
        loadBidItemImage();
    }

    public void setItemName(String item){
        mItemName.setText(item);
    }

    public void setItemOwner(String owner){
        mItemOwner.setText(getResources().getString(R.string.auction_owner_display, owner));
    }

    private void loadBidItemImage(){

        Picasso.get().load(mImageURL).fit().centerCrop().into(mItemImage);
    }

}
