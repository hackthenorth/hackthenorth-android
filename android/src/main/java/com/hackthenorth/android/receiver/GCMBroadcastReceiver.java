package com.hackthenorth.android.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hackthenorth.android.HackTheNorthApplication;
import com.hackthenorth.android.R;
import com.hackthenorth.android.framework.HTNNotificationManager;
import com.hackthenorth.android.framework.HTTPFirebase;
import com.hackthenorth.android.framework.VisibilityManager;
import com.hackthenorth.android.ui.MainActivity;

public class GCMBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GCMBroadcastReceiver";

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
            // Check if an activity is visible. If so, then don't show the notification at
            // all; just update the list in realtime.
            // TODO: Might want to do something different if they have a fragment that
            // TODO: isn't the updates list open, like a little shake in the UI or something.
            if (VisibilityManager.isActivityVisible()) {

                HTTPFirebase.GET("/updates", context,
                        HackTheNorthApplication.Actions.SYNC_UPDATES);

                // Otherwise, show a notification.
            } else {

                // For simplicity, just give the user a notification, and have them
                // sync up with the new update when they open the app. In the future,
                // we may want to perform the HTTP requests here before showing the
                // user a notification (but we may have to use a Service instead, in
                // that case)
                HTNNotificationManager.sendUpdateNotification(context, intent);
            }
        }
    }
}
