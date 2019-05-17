package com.example.paceexchange;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.paceexchange.FirebaseCloudMessenger.MessageService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BidInAuction extends AppCompatActivity {
    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseInventoryCollection;
    private CollectionReference mFirebaseAuctionInventoryCollection;

    private ArrayList<InventoryData> mCurrentInventorylist;
    private Spinner mInventoryItemSpinner;
    private  ArrayAdapter<String> mSpinnerArrayAdapter;
    private int mCurrentItemPosition;
    private String mAuctionKey;

    private TextView mAuctionTitleTextView, mAuctionCategoryTextView, mAuctionTradeInForTextView, mAuctionPostedByTextView;
    private CircleImageView mAuctionItemImageCircleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid_in_auction);
        mAuctionItemImageCircleView = findViewById(R.id.itemImage);
        mAuctionTitleTextView = findViewById(R.id.itemName);
        mAuctionCategoryTextView = findViewById(R.id.itemType);
        mAuctionTradeInForTextView = findViewById(R.id.itemTradeFor);
        mAuctionPostedByTextView = findViewById(R.id.itemPostedBy);
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");
        mFirebaseAuctionInventoryCollection = mFirebaseDatabase.collection("auctionInventory");
        mCurrentInventorylist = new ArrayList<>();
        mInventoryItemSpinner = findViewById(R.id.inventoryItemSpinner);
        Intent intent = getIntent();
        mAuctionKey = intent.getStringExtra(MessageService.AUCTION_ID);
        getAuctionItemsDetails();

        getUsersCurrentFirebaseInventory();
    }

    private void getAuctionItemsDetails() {
        DocumentReference docRef = mFirebaseAuctionInventoryCollection.document(mAuctionKey);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mAuctionTitleTextView.setText(document.getData().get("title").toString());
                        mAuctionCategoryTextView.setText(document.getData().get("category").toString());
                        mAuctionTradeInForTextView.setText(document.getData().get("tradeInFor").toString());
                        mAuctionPostedByTextView.setText( document.getData().get("username").toString());
                        Picasso.get().load(document.getData().get("url").toString()).fit().centerCrop().into(mAuctionItemImageCircleView);
                    }
                } else {
                    Log.d("Fetch_Failed", task.getException()+"");
                }
            }
        });
    }




    public void getUsersCurrentFirebaseInventory() {

        mFirebaseInventoryCollection.document(LoggedInUser.getInstance().getmLoogedInUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Map<String, Object> map = task.getResult().getData();
                if (map.get("Items") != null) {
                    ArrayList<Object> newArray = (ArrayList<Object>) map.get("Items");
                    storeUserCurrentInventory(newArray);
                }
            }
        });
    }

    private void storeUserCurrentInventory(ArrayList<Object> list) {

        for (int i = 0; i < list.size(); i++) {

            JSONArray arr = new JSONArray(list);
            JSONObject json = arr.optJSONObject(i);
            String tradeIn = json.optString("tradeInFor");
            String category = json.optString("category");
            String title = json.optString("title");
            String itemID = json.optString("itemID");
            String url = json.optString("url");
            String tag = json.optString("tag");

            mCurrentInventorylist.add(new InventoryData(category, title, tradeIn, itemID, url, tag));
        }

        String[] itemTitle = new String[mCurrentInventorylist.size()];
        for(int i=0;i<mCurrentInventorylist.size();i++)
        {
            itemTitle[i] = mCurrentInventorylist.get(i).getTitle();
        }

        mSpinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        itemTitle); //selected item will look like a spinner set from XML
        mSpinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        mInventoryItemSpinner.setAdapter(mSpinnerArrayAdapter);
        mInventoryItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentItemPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    public void addToButton(View view) {
        Log.d("item",mCurrentInventorylist.get(mCurrentItemPosition).toString());
        addItemToBid();
    }

    public void addItemToBid(){
        InventoryData data = mCurrentInventorylist.get(mCurrentItemPosition);
        SaveBidInAuctionPojo bidInAuctionPojoObject = new SaveBidInAuctionPojo(data.getCategory(),data.getItemID(),data.getTitle(),data.getUrl(), data.getTradeInFor(), data.getTag(),LoggedInUser.getInstance().getmLoogedInUser());
        Log.d("data",""+data);
        mFirebaseAuctionInventoryCollection.document(mAuctionKey).update("bids", FieldValue.arrayUnion(bidInAuctionPojoObject));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
