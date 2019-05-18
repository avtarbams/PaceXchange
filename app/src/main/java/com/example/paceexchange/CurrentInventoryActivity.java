package com.example.paceexchange;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;


public class CurrentInventoryActivity extends AppCompatActivity {

    private Button mAddNewItemButton;

    private FirebaseFirestore mFireBaseDatabase;
    private CollectionReference mFireBaseInventoryCollection;

    private RecyclerView mRecyclerView;
    private RecyclerAdapter mAdapter;
    private ArrayList<InventoryData> mCurrentInventorylist;
    private int mRowClickPosition;
    private String mCurrentItemSelectionID;
    private String mUserIdentification;

    private Dialog mDeleteDialog;

    public static final String USER_IDENTIFICATION_ADD_ITEM_MESSAGE = "com.example.paceexchange.USERID";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        mCurrentInventorylist = new ArrayList<>();

        Intent intent = getIntent();
        mUserIdentification = intent.getStringExtra(UserProfileActivity.USER_IDENTIFICATION_INVENTORY_MESSAGE);
        mAddNewItemButton = findViewById(R.id.addInventoryItemButton);

        mFireBaseDatabase = FirebaseFirestore.getInstance();
        mFireBaseInventoryCollection = mFireBaseDatabase.collection("inventory");

        mDeleteDialog = new Dialog(this);

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
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, 0));
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(mRecyclerView);
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

    }

    public void getUsersCurrentFirebaseInventory() {

        mFireBaseInventoryCollection.document(mUserIdentification).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
            mAdapter.notifyDataSetChanged();
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

    /*** Swipe Feature implementation ***/
    ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            mDeleteDialog.setContentView(R.layout.delete_modal);
            Button mDeleteModalYes = mDeleteDialog.findViewById(R.id.delete_modal_yes_button);
            Button mDeleteModalNo = mDeleteDialog.findViewById(R.id.delete_modal_no_button);
            // On Declined
            mDeleteModalNo.setOnClickListener(V-> {
                mAdapter.notifyDataSetChanged();
                mDeleteDialog.dismiss();
            });
            // On Agreed
            mDeleteModalYes.setOnClickListener(V-> {
                mCurrentInventorylist.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyDataSetChanged();
                mFireBaseInventoryCollection.document(mUserIdentification).update("Items", FieldValue.delete());

                for (InventoryData object : mCurrentInventorylist) {
                    mFireBaseInventoryCollection.document(mUserIdentification).update("Items", FieldValue.arrayUnion(object));
                }
                mDeleteDialog.dismiss();
            });
            mDeleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mDeleteDialog.show();
        }
    };

}