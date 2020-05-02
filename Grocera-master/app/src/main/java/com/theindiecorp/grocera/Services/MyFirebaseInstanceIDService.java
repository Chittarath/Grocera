package com.theindiecorp.grocera.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.theindiecorp.grocera.LoginActivity;
import com.theindiecorp.grocera.R;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseIIDService";
    String channelId = "SOME_CHANNEL";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";
        try{
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }catch (NullPointerException e){
            Log.e(TAG, "onMessageReceived: NullPointerException: " + e.getMessage() );
        }
        Log.d(TAG, "onMessageReceived: data: " + notificationData);
        Log.d(TAG, "onMessageReceived: notification body: " + notificationBody);
        Log.d(TAG, "onMessageReceived: notification title: " + notificationTitle);


        String dataType = remoteMessage.getData().get("data_type");
        if(dataType.equals("direct_message")){
            Log.d(TAG, "onMessageReceived: new incoming message.");
            String title = remoteMessage.getData().get("type").toUpperCase();
            String message = remoteMessage.getData().get("message");
            String messageId = remoteMessage.getData().get("message_id");
            sendMessageNotification(title, message, messageId);
        }
    }

    private void sendMessageNotification(String title, String message, String messageId) {

        //get the notification id
        int notificationId = buildNotificationId(messageId);

        // Creates an Intent for the Activity
        Intent intent = new Intent(this, LoginActivity.class);
        // Sets the Activity to start in a new, empty task

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0 /* request code */, intent,PendingIntent.FLAG_UPDATE_CURRENT);

        channelId = "channel_1";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){

            CharSequence name = "new_channel";
            String Description = "This is a channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 100});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =  new NotificationCompat.Builder(this,channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setLights(Color.BLUE,1,1)
                .setContentIntent(pendingIntent);

        notificationManager.notify(234 /* ID of notification */, notificationBuilder.build());
    }

    private int buildNotificationId(String id) {
        Log.d(TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Log.d(TAG, "buildNotificationId: id: " + id);
        Log.d(TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sendRegistrationToken(s);
    }

    public void sendRegistrationToken(String token){
        Log.d(TAG, "sendRegistrationToken: " + token);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("messageTokens")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
        }
    }
}
