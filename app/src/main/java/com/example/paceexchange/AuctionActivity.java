package com.example.paceexchange;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class AuctionActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private AuctionFragment mItemDisplay, itemFragment, fragment;
    private Handler mainThreadHandler;
    private Button mStartBidButton, mNextItemButton;
    private TextView mText, mUserBidItem;
    private int mTimer = 60;
    public static final String BID_ITEM_MESSAGE = "com.example.paceexchange.ITEMMESSAGE";
    private String mUserIdentification;
    ArrayList<InventoryData> inventoryDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auction);

        Intent intent = getIntent();
        mUserIdentification = intent.getStringExtra(UserProfileActivity.USER_IDENTIFICATION_INVENTORY_MESSAGE);

        inventoryDataList = new ArrayList<>();

        mUserBidItem = findViewById(R.id.userBidItem);
        mStartBidButton = findViewById(R.id.startButton);
        mNextItemButton = findViewById(R.id.nextItemButton);
        mText = findViewById(R.id.number);

        mainThreadHandler = new Handler(Looper.getMainLooper());


        mItemDisplay = new AuctionFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.itemContainer, mItemDisplay).commit();

        fragment = new AuctionFragment();

        setButtonClickListeners();
    }


    public void setButtonClickListeners() {

        mNextItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                itemFragment = new AuctionFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.itemContainer, itemFragment).commit();
            }
        });

        mStartBidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountdown();
            }
        });

    }

    public void startCountdown() {

        final Thread mClock = new Thread() {
            @Override
            public void run() {

                mTimer--;

                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        mText.setText(getResources().getString(R.string.seconds_display, String.valueOf(mTimer)));

                        if (mTimer >= 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            startCountdown();
                        } else {
                            mText.setText(getResources().getString(R.string.auction_ended));
                        }
                    }
                });

            }

        };

        mClock.start();

    }
}



