package com.example.paceexchange;


import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FirebaseDataMaintenanceHelper {

    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseInventoryCollection;
    private RecyclerAdapter mAdapter;

    private ArrayList<InventoryData> mUserInventoryList;
    private String mUserIdentification;

    FirebaseDataMaintenanceHelper(RecyclerAdapter adapter) {

        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");

        mUserInventoryList = new ArrayList<>();

        mAdapter=adapter;
    }

    public void getUsersCurrentFirebaseInventory(String userID) {

        mFirebaseInventoryCollection.document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Map<String, Object> map = task.getResult().getData();
                if (map.get("Items") != null) {
                    ArrayList<Object> newArray = (ArrayList<Object>) map.get("Items");
                    Iterator i = newArray.iterator();
                    while (i.hasNext()) {
                        Log.d("AVTAR", i.next() + "");
                    }

                    storeUserCurrentInventory(newArray);
                }
            }
        });
    }

    public void storeUserCurrentInventory(ArrayList<Object> list) {

        for (int i = 0; i < list.size(); i++) {
            JSONArray arr = new JSONArray(list);
            JSONObject json = arr.optJSONObject(i);
            String tradeIn = json.optString("tradeInFor");
            String category = json.optString("category");
            String title = json.optString("title");
            String itemID = json.optString("itemID");
            String url = json.optString("url");
            String tag = json.optString("tag");

            mUserInventoryList.add(new InventoryData(category, title, tradeIn, itemID, url, tag));
            mAdapter.notifyDataSetChanged();
        }

        Log.d("INACIO CLASS", String.valueOf(mUserInventoryList.size()));
    }

    public ArrayList<InventoryData> getCurrentInventoryArrayList(){

        Log.d("INACIO CLASS", String.valueOf(mUserInventoryList.size()));
        return mUserInventoryList;
    }



}