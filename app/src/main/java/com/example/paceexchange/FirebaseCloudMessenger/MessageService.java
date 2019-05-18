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
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;

public class MessageService extends FirebaseMessagingService {

    private NotificationChannel mNotificationChannel;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private String android_channel_id = "com.example.paceexchange.test";
    public static final String AUCTION_ID = "com.example.paceexchange.auction";

    //generates a registration token for the client app instance
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }

    //message handling upon receipt from Google FCM for display to user
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
       // displayFirebaseMessage(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

        if (remoteMessage.getData().isEmpty()) {
            displayForegroundFirebaseMessage(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else {
           displayBackgroundFirebaseMessage(remoteMessage.getData());
        }
    }

    //**BACKGROUND/KILLED APPLICATION DELIVERY**
    //set notification content and establish channel with NotificationChannel and NotificationCompat.Builder objects
    private void displayBackgroundFirebaseMessage(Map<String, String> data) {

        String title = data.get("title");
        String body = data.get("itemId");


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationChannel = new NotificationChannel(android_channel_id, "Notification", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationChannel.setDescription("PACE EXCHANGE AUCTION INVITATION");
        mNotificationChannel.enableLights(true);
        mNotificationChannel.enableVibration(true);
        mNotificationChannel.setLightColor(Color.RED);

        mNotificationManager.createNotificationChannel(mNotificationChannel);

        //build notification message with relevant attributes (title, text, icon, etc.)
        mNotificationBuilder = new NotificationCompat.Builder(this, android_channel_id)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_announcement_black_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("info");
        Intent intent = new Intent(getApplicationContext(), BidInAuction.class);
        intent.putExtra(MessageService.AUCTION_ID,data.get("itemId"));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(pendingIntent);

        //tell notification manager to build the message for display to user
        mNotificationManager.notify(new Random().nextInt(), mNotificationBuilder.build());
    }

    //**FOREGROUND APPLICATION DELIVERY**
    //set notification content and establish channel with NotificationChannel and NotificationCompat.Builder objects
    private void displayForegroundFirebaseMessage(String title, String body) {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationChannel = new NotificationChannel(android_channel_id, "Notification", NotificationManager.IMPORTANCE_DEFAULT);

        mNotificationChannel.setDescription("PACE EXCHANGE AUCTION INVITATION");
        mNotificationChannel.enableLights(true);
        mNotificationChannel.enableVibration(true);
        mNotificationChannel.setLightColor(Color.RED);

        mNotificationManager.createNotificationChannel(mNotificationChannel);


        //build notification message with relevant attributes (title, text, icon, etc.)
        mNotificationBuilder = new NotificationCompat.Builder(this, new Random().nextInt() + "")
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_announcement_black_24dp)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info");

        //tell notification manager to build the message for display to user
        mNotificationManager.notify(new Random().nextInt(), mNotificationBuilder.build());
    }

}

