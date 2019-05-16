package com.example.paceexchange;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CurrentInventoryActivity extends AppCompatActivity {

    private Button mTradeItemButton, mAddNewItemButton, mRemoveItemButton;

    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseInventoryCollection;
    private FirebaseDataMaintenanceHelper mFirebaseMaintainer;

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private ArrayList<InventoryData> mCurrentInventorylist;
    private int mRowClickPosition;
    private String mCurrentItemSelectionID;
    private String mUserIdentification;

    public static final String USER_IDENTIFICATION_ADD_ITEM_MESSAGE = "com.example.paceexchange.USERID";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        mCurrentInventorylist = new ArrayList<>();

        Intent intent = getIntent();
        mUserIdentification = intent.getStringExtra(UserProfileActivity.USER_IDENTIFICATION_INVENTORY_MESSAGE);

        mTradeItemButton = findViewById(R.id.tradeSelectedItemButton);
        mAddNewItemButton = findViewById(R.id.addInventoryItemButton);
        mRemoveItemButton = findViewById(R.id.removeInventoryItemButton);

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");

        setButtonClickListener();
        getUsersCurrentFirebaseInventory();
        setRecyclerView();
    }

    public void setRecyclerView() {

        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new RecyclerAdapter(CurrentInventoryActivity.this, mCurrentInventorylist, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRowClickPosition = (int) v.getTag();
                InventoryData display = mAdapter.getItem(mRowClickPosition);
                mCurrentItemSelectionID = display.getItemID();
            }

        });

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.row_divider_line, null));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

    }

    public void setButtonClickListener() {

        mAddNewItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AddInventoryItemActivity.class);
                intent.putExtra(USER_IDENTIFICATION_ADD_ITEM_MESSAGE, mUserIdentification);
                finish();
                startActivity(intent);
            }
        });

        mRemoveItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentItemSelectionID == null) {
                    Toast.makeText(getApplicationContext(), R.string.remove_error, Toast.LENGTH_LONG).show();
                } else {
                    removeItemFromInventory();
                }
            }
        });

        mTradeItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(CurrentInventoryActivity.this, AuctionActivity.class);
                //intent.putExtra(EXTRA_MESSAGE_TRADE_ITEM, mAdapter.getItem(mRowClickPosition).getmItemName());
                //intent.putExtra(EXTRA_MESSAGE_TRADE_ITEM_VALUE, mAdapter.getItem(mRowClickPosition).getmItemValue());
                startActivity(intent);

            }
        });
    }

    public void getUsersCurrentFirebaseInventory() {

        mFirebaseInventoryCollection.document(mUserIdentification).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Map<String, Object> map = task.getResult().getData();
                if (map.get("Items") != null) {
                    ArrayList<Object> newArray = (ArrayList<Object>) map.get("Items");
                    Iterator i = newArray.iterator();
                    while (i.hasNext()) {
                        Log.d("AVTAR", i.next() + "");
                        //  Object firstKey = map.keySet().toArray()[0];
                        //  Object valueForFirstKey = map.get(firstKey);

                    }

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
            mAdapter.notifyDataSetChanged();
        }

    }

    private void removeItemFromInventory() {

        mCurrentInventorylist.remove(mRowClickPosition);
        mAdapter.notifyDataSetChanged();

        mFirebaseInventoryCollection.document(mUserIdentification).update("Items", FieldValue.delete());

        for (InventoryData object : mCurrentInventorylist) {
            mFirebaseInventoryCollection.document(mUserIdentification).update("Items", FieldValue.arrayUnion(object));
        }

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