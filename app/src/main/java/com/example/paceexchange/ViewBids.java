package com.example.paceexchange;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ViewBids extends AppCompatActivity {

    private TextView mBidTimerTextView;
    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseAuctionInventory;
    private CollectionReference mFirebaseInventoryCollection;

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private ArrayList<InventoryData> mCurrentBidlist;
    private ArrayList<InventoryData> mSellerInventoryList;
    private Dialog mAcceptDialog;
    private int mRowClickPosition;
    private ArrayList<SaveBidInAuctionPojo> mDataList;
    private ArrayList<InventoryData> mSellerInventory;
    private String mCurrentItemSelectionID;
    String mUsername;
    String mSellerUserName;
    String mAuctionDocumentNumber;


    private InventoryData mSelectedBidder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bids);
        mBidTimerTextView = findViewById(R.id.bidCollectionTimerTextView);
        startTimer();
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseAuctionInventory = mFirebaseDatabase.collection("auctionInventory");
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");
        Intent intent = getIntent();
        mAuctionDocumentNumber = intent.getStringExtra(AddAuctionItemActivity.AUCTION_DOCUMENT);
        mCurrentBidlist = new ArrayList<InventoryData>();
        mSellerInventory = new ArrayList<InventoryData>();
        mAcceptDialog = new Dialog(this);
        mDataList = new ArrayList<SaveBidInAuctionPojo>();
        mSellerInventoryList = new ArrayList<InventoryData>();

    }

    private void setRecyclerView() {

        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new RecyclerAdapter(getApplicationContext(), mCurrentBidlist, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRowClickPosition = (int) v.getTag();
                InventoryData display = mAdapter.getItem(mRowClickPosition);
                mCurrentItemSelectionID = display.getItemID();
            }

        });
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, 0));
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    /*startTimer() will call the ontick after each second and after the count down if finished,
    onFinish() is called which will then get the data from firebase for bids placed on the auction  item
    */
    public void startTimer(){
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                mBidTimerTextView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mBidTimerTextView.setText("done!");
                getAuctionDataFromFirebase();
            }
        }.start();
    }

    public void getSellerInventoryData(String username) {

        mFirebaseInventoryCollection.document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Map<String, Object> map = task.getResult().getData();
                if (map.get("Items") != null) {
                    ArrayList<Object> newArray = (ArrayList<Object>) map.get("Items");
                    storeSellerData(newArray);
                }
            }
        });
    }

    private void storeSellerData(ArrayList<Object> list) {

        for (int i = 0; i < list.size(); i++) {

            JSONArray arr = new JSONArray(list);
            JSONObject json = arr.optJSONObject(i);
            String tradeIn = json.optString("tradeInFor");
            String category = json.optString("category");
            String title = json.optString("title");
            String itemID = json.optString("itemID");
            String url = json.optString("url");
            String tag = json.optString("tag");
            mSellerInventory.add(new InventoryData(category, title, tradeIn, itemID, url, tag));
        }

        updateCollections(mSelectedBidder);

    }

    //gets the auction data through firebase
    public void getAuctionDataFromFirebase() {
        mFirebaseAuctionInventory.document(mAuctionDocumentNumber).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Map<String, Object> map = task.getResult().getData();
                //items needs to be replaced with the field ID below
                if (map.get("bids") != null) {
                    ArrayList<Object> newArray = (ArrayList<Object>) map.get("bids");
                    storeAuctionData(newArray);
                }
                if(map.get("username") != null )
                {
                    mUsername = (String)map.get("username");
                }
            }
        });
    }

    private void storeAuctionData(ArrayList<Object> list) {
        for (int i = 0; i < list.size(); i++) {
            JSONArray arr = new JSONArray(list);
            JSONObject json = arr.optJSONObject(i);
            String category = json.optString("category");
            String title = json.optString("title");
            String itemID = json.optString("itemID");
            String tag = json.optString("tag");
            String tradeInFor = json.optString("tradeInFor");
            String url = json.optString("url");
            String username = json.optString("username");

            mCurrentBidlist.add(new InventoryData(category, title, tradeInFor, itemID, url, tag));
            mDataList.add(new SaveBidInAuctionPojo(category,itemID, title,url, tradeInFor, tag,username));
        }
        setRecyclerView();
    }

    ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            mAcceptDialog.setContentView(R.layout.accept_bid_modal);
            Button mAcceptModalYes = mAcceptDialog.findViewById(R.id.delete_modal_yes_button);
            Button mAcceptModalNo = mAcceptDialog.findViewById(R.id.delete_modal_no_button);

            mAcceptModalNo.setOnClickListener(V-> {
                mAdapter.notifyDataSetChanged();
                mAcceptDialog.dismiss();
            });

            //accept bid
            mAcceptModalYes.setOnClickListener(V-> {
                //data transaction code here
                int position = viewHolder.getAdapterPosition();
                SaveBidInAuctionPojo object = mDataList.get(position);
                mSellerUserName = object.getUsername();
                InventoryData inventoryData = new InventoryData(object.getCategory(), object.getTitle(), object.getTradeInFor(), object.getItemID(), object.getUrl(), object.getTag());
                mSelectedBidder = inventoryData;
                getSellerInventoryData(object.getUsername());
                mAcceptDialog.dismiss();
            });
            mAcceptDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mAcceptDialog.show();

        }
    };


    // Logic for adding to to Auctioner's Inventory and removing through bidder
    public void updateCollections(InventoryData object){
        mSellerInventory.removeIf(data -> data.getItemID().equalsIgnoreCase(object.getItemID()));
        mFirebaseInventoryCollection.document(mSellerUserName).update("Items",FieldValue.delete());
        for (InventoryData object1 : mSellerInventory) {
            mFirebaseInventoryCollection.document(mSellerUserName).update("Items", FieldValue.arrayUnion(object1));
        }
        mFirebaseInventoryCollection.document(mUsername).update("Items", FieldValue.arrayUnion(object));
    }


}
