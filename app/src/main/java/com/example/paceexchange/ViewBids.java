package com.example.paceexchange;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bids);
        mBidTimerTextView = findViewById(R.id.bidCollectionTimerTextView);
        startTimer();
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseAuctionInventory = mFirebaseDatabase.collection("auctionInventory");

    }

    public void startTimer(){
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                mBidTimerTextView.setText("seconds remaining: " + millisUntilFinished / 1000);
                getAuctionDataFromFirebase();
            }

            public void onFinish() {
                mBidTimerTextView.setText("done!");
               // getAuctionDataFromFirebase();
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
                    Log.d("newArray",newArray.toString());

                    storeAuctionData(newArray);
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
            String itemID = json.optString("itemID");
            String tag = json.optString("tag");
            String title = json.optString("tradeInFor");
            String tradeInFor = json.optString("url");
            String username = json.optString("username");
            Log.d("json",""+category+itemID+tag+title+tradeInFor+username);
            //create your arraylist here
//            mAuctionDatalist.add(new AuctionData(category, itemID, tag, title, tradeInFor));
//            mAdapter.notifyDataSetChanged();
        }
    }

}
