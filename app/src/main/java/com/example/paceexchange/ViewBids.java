package com.example.paceexchange;

import android.app.Dialog;
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
    private Dialog mAcceptDialog;
    private int mRowClickPosition;
    private ArrayList<SaveBidInAuctionPojo> mDataList;
    private String mCurrentItemSelectionID;
    String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bids);
        mBidTimerTextView = findViewById(R.id.bidCollectionTimerTextView);
        startTimer();
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseAuctionInventory = mFirebaseDatabase.collection("auctionInventory");
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");

        mCurrentBidlist = new ArrayList<InventoryData>();
        mAcceptDialog = new Dialog(this);
        mDataList = new ArrayList<SaveBidInAuctionPojo>();
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

    public void startTimer(){
        new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
                mBidTimerTextView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mBidTimerTextView.setText("done!");
                getAuctionDataFromFirebase();
            }
        }.start();
    }


    public void getAuctionDataFromFirebase() {
        mFirebaseAuctionInventory.document("cuFM1OcnXRvWfG8tl6Ol").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    Log.d("username",mUsername);
                }
            }
        });
    }
    //you need to do something like this...but I do not know your keys in the auction database because it looks like they were replaced over
    private void storeAuctionData(ArrayList<Object> list) {
        for (int i = 0; i < list.size(); i++) {
            //"json.optString("*****"); --- below insert whatever the key is...this example is from how we did it for userprofile and inventory
            JSONArray arr = new JSONArray(list);
            JSONObject json = arr.optJSONObject(i);
            String category = json.optString("category");
            String title = json.optString("title");
            String itemID = json.optString("itemID");
            String tag = json.optString("tag");
            String tradeInFor = json.optString("tradeInFor");
            String url = json.optString("url");
            String username = json.optString("username");
            Log.d("jsonArray",""+category+title+itemID+tag+tradeInFor+url+username);

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
            Button mDeleteModalYes = mAcceptDialog.findViewById(R.id.delete_modal_yes_button);
            Button mDeleteModalNo = mAcceptDialog.findViewById(R.id.delete_modal_no_button);

            mDeleteModalNo.setOnClickListener(V-> {
                mAdapter.notifyDataSetChanged();
                mAcceptDialog.dismiss();
            });

            mDeleteModalYes.setOnClickListener(V-> {
                mAcceptDialog.dismiss();
                //data transaction code here
                int position = viewHolder.getAdapterPosition();
                SaveBidInAuctionPojo object = mDataList.get(position);

                Log.d("object",object.toString());
                mFirebaseInventoryCollection.document(object.getUsername()).update("Items",FieldValue.arrayRemove(object));
                mFirebaseInventoryCollection.document(mUsername).update("Items",FieldValue.arrayUnion(object));

               // mFirebaseAuctionInventoryCollection.document(mAuctionKey).update("bids", FieldValue.arrayUnion(bidInAuctionPojoObject));



            });
            mAcceptDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mAcceptDialog.show();

        }
    };

}
