package com.kakao.sdk.sample.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kakao.sdk.sample.MainActivity;
import com.kakao.sdk.sample.R;

/**
 * @author kevin.kang
 * Created by kevin.kang on 2017. 1. 26..
 */

public class SampleFirebaseMessagingService extends FirebaseMessagingService {
    public static final int NOTIFICATION_ID = 1;
    public static int count = 0;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("프로세스","샘플파이어베이스메시징서비스onMessageReceived");

        super.onMessageReceived(remoteMessage);
        sendDataMessage(remoteMessage);
    }

    private void sendDataMessage(final RemoteMessage message) {
        Log.d("프로세스","샘플파이어베이스메시징서비스sendDataMessage");
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "kakao_push_channel")
                .setSmallIcon(R.drawable.push_noti_icon)
                .setContentTitle(getApplicationContext().getString(R.string.push_notification_title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message.getData().get("content")))
                .setContentText(message.getData().get("content"));
                Log.d("TAG","여기서 확인맞쥬.,?");

        mBuilder.setContentIntent(contentIntent);
        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION_ID + count++, mBuilder.build());
        }
    }
}