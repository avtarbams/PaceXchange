package com.example.paceexchange.FirebaseCloudMessenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.paceexchange.AuctionActivity;
import com.example.paceexchange.BidInAuction;
import com.example.paceexchange.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MessageService extends FirebaseMessagingService {

    private NotificationChannel mNotificationChannel;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private String android_channel_id = "com.example.paceexchange.test";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.d("TOKEN FIREBASE", s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().isEmpty()) {
            displayFirebaseMessage(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else {
            displayFirebaseMessage(remoteMessage.getData());
        }
    }


    private void displayFirebaseMessage(Map<String, String> data) {

        String title = data.get("title");
        String body = data.get("itemId");


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationChannel = new NotificationChannel(android_channel_id, "Notification", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationChannel.setDescription("PACE EXCHANGE AUCTION INVITATION");
        mNotificationChannel.enableLights(true);
        mNotificationChannel.enableVibration(true);
        mNotificationChannel.setLightColor(Color.RED);

        mNotificationManager.createNotificationChannel(mNotificationChannel);

        mNotificationBuilder = new NotificationCompat.Builder(this, android_channel_id)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_announcement_black_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info");
        Intent intent = new Intent(getApplicationContext(), BidInAuction.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent, Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        mNotificationBuilder.setContentIntent(pendingIntent);

        mNotificationManager.notify(new Random().nextInt(), mNotificationBuilder.build());
    }

    private void displayFirebaseMessage(String title, String body) {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationChannel = new NotificationChannel(android_channel_id, "Notification", NotificationManager.IMPORTANCE_DEFAULT);

        mNotificationChannel.setDescription("PACE EXCHANGE AUCTION INVITATION");
        mNotificationChannel.enableLights(true);
        mNotificationChannel.enableVibration(true);
        mNotificationChannel.setLightColor(Color.RED);

        mNotificationManager.createNotificationChannel(mNotificationChannel);

        mNotificationBuilder = new NotificationCompat.Builder(this, android_channel_id)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_announcement_black_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info");

        mNotificationManager.notify(new Random().nextInt(), mNotificationBuilder.build());
    }
}
