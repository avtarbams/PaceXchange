package com.example.paceexchange;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ItemsForTradeActivity extends AppCompatActivity {

    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseInventoryCollection;
    private String mSelectedUserIdentification, mUserIdentification;
    private ArrayList<InventoryData> mAllItemsList;
    private ArrayList<InventoryData> mFilteredList;
    private String mCurrentItemSelectionID;
    private int mRowClickPosition;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private Button mAIButoon;

    private Button mClearButton;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_trade_items);

        Intent intent = getIntent();
        mUserIdentification = intent.getStringExtra(CurrentInventoryActivity.USER_IDENTIFICATION_ADD_ITEM_MESSAGE);

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");


        mClearButton = findViewById(R.id.clearFilter);
        mClearButton.setOnClickListener(V-> {
            setRecyclerView();
            mFilteredList.clear();
            mAIButoon.setText(getText(R.string.SearchAI));
        });

        mAllItemsList = new ArrayList<>();
        mFilteredList = new ArrayList<>();

        setRecyclerView();
        getAllFireStoreUserIDs();

    }

    public void getAllFireStoreUserIDs() {


        mFirebaseInventoryCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        mSelectedUserIdentification = document.getId();
                        Log.d("KEITH", mSelectedUserIdentification);
                        Log.d("KEITH", mUserIdentification);

                        if(!mSelectedUserIdentification.equals(mUserIdentification)) {
                            getUsersCurrentFirebaseInventory(mSelectedUserIdentification);
                        }

                        //Log.d("KEITH", document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d("KEITH", "Error getting documents: ", task.getException());
                }
            }
        });


    }

    public void getUsersCurrentFirebaseInventory(String id) {

        mFirebaseInventoryCollection.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Map<String, Object> map = task.getResult().getData();
                if (map.get("Items") != null) {
                    ArrayList<Object> newArray = (ArrayList<Object>) map.get("Items");
                    storeUniversalInventory(newArray);
                }
            }
        });
    }

    private void storeUniversalInventory(ArrayList<Object> list) {

        for (int i = 0; i < list.size(); i++) {

            JSONArray arr = new JSONArray(list);
            JSONObject json = arr.optJSONObject(i);
            String tradeIn = json.optString("tradeInFor");
            String category = json.optString("category");
            String title = json.optString("title");
            String itemID = json.optString("itemID");
            String url = json.optString("url");
            String tag = json.optString("tag");


            mAllItemsList.add(new InventoryData(category, title, tradeIn, itemID, url, tag));
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setRecyclerView() {

        mRecyclerView = findViewById(R.id.recyclerView);
        mAdapter = new RecyclerAdapter(ItemsForTradeActivity.this, mAllItemsList, v -> {

            mRowClickPosition = (int) v.getTag();
            InventoryData display = mAdapter.getItem(mRowClickPosition);
            mCurrentItemSelectionID = display.getItemID();
        });

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, 0));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

    }

    public void checkItemAI(View view) {
        mAIButoon = view.findViewById(R.id.AIbutton);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                    .getCloudImageLabeler();
            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            mAIButoon.setText(labels.get(0).getText());
                            for(int i=0; i<mAllItemsList.size();i++){
                                if(mAllItemsList.get(i).getTag().equalsIgnoreCase(labels.get(0).getText())){
                                    mFilteredList.add(mAllItemsList.get(i));
                                }
                            }
                            mAdapter = new RecyclerAdapter(ItemsForTradeActivity.this, mFilteredList, v -> {
                                mRowClickPosition = (int) v.getTag();
                                InventoryData display = mAdapter.getItem(mRowClickPosition);
                                mCurrentItemSelectionID = display.getItemID();
                            });
                            mRecyclerView.setAdapter(mAdapter);
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
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
