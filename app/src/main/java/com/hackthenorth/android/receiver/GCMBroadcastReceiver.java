package com.hackthenorth.android.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.ui.MainActivity;

public class GCMBroadcastReceiver extends BroadcastReceiver {

    public GCMBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // This is all copied from the GCM client implementation tutorial [1].
        // [1]: https://developer.android.com/google/gcm/client.html
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty() &&
                GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

            // For simplicity, just give the user a notification, and have them
            // sync up with the new update when they open the app. In the future,
            // we may want to perform the HTTP requests here before showing the
            // user a notification (but we may have to use a Service instead, in
            // that case)
            sendNotification(context, intent);
        }
    }

    private void sendNotification(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        String title = extras.getString("name", "Hack The North");
        String message = extras.getString("description", "Click here for a new update!");

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                .setContentText(message);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(HackTheNorthApplication.NOTIFICATIONS_ID,
                builder.build());
    }
}
