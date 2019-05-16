package com.example.paceexchange;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

public class BidInAuction extends AppCompatActivity {
    private FirebaseFirestore mFirebaseDatabase;
    private CollectionReference mFirebaseInventoryCollection;
    private ArrayList<InventoryData> mCurrentInventorylist;
    private Spinner mInventoryItemSpinner;
    private  ArrayAdapter<String> mSpinnerArrayAdapter;
    private int mCurrentItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid_in_auction);
        mFirebaseDatabase = FirebaseFirestore.getInstance();
        mFirebaseInventoryCollection = mFirebaseDatabase.collection("inventory");
        mCurrentInventorylist = new ArrayList<>();
        mInventoryItemSpinner = findViewById(R.id.inventoryItemSpinner);

        getUsersCurrentFirebaseInventory();
    }



    public void getUsersCurrentFirebaseInventory() {

        mFirebaseInventoryCollection.document(LoggedInUser.getInstance().getmLoogedInUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
        }

        String[] itemTitle = new String[mCurrentInventorylist.size()];
        for(int i=0;i<mCurrentInventorylist.size();i++)
        {
            itemTitle[i] = mCurrentInventorylist.get(i).getTitle();
            Log.d("array",itemTitle[i]);
        }
        Log.d("Arraylist",mCurrentInventorylist.toString());


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
    }
}
