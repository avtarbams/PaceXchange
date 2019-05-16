package com.example.paceexchange;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ViewBids extends AppCompatActivity {

    private TextView mBidTimerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bids);
        mBidTimerTextView = findViewById(R.id.bidCollectionTimerTextView);
       startTimer();
    }

    public void startTimer(){
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                mBidTimerTextView.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mBidTimerTextView.setText("done!");
            }
        }.start();
    }
}
