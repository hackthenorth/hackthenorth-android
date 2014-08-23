package com.hackthenorth.android.framework;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

import com.hackthenorth.android.R;
import com.hackthenorth.android.model.Update;
import com.hackthenorth.android.ui.MainActivity;
import com.hackthenorth.android.ui.settings.SettingsFragment;

import java.util.LinkedList;
import java.util.List;

public class HTNNotificationManager {

    private static final String TAG = "HTNNotificationManager";
    public static final int UPDATES_NOTIFICATION_ID = 1;

    private static final List<Update> mUpdates = new LinkedList<Update>();

    public static void clearUpdatesNotification(Context context) {
        mUpdates.clear();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(UPDATES_NOTIFICATION_ID);
    }

    public static void sendUpdateNotification(Context context, Intent intent) {

        // Get the user's preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notifications = prefs.getBoolean(SettingsFragment.PREF_NOTIFS, true);

        if (notifications) {
            // Add the update to the list, and then build the notification style
            mUpdates.add(0, Update.fromBundle(intent.getExtras()));

            Notification.Style style;
            String title;
            String contentText;

            // If we have only one notification, use BigTextStyle.
            if (mUpdates.size() == 1) {

                title = mUpdates.get(0).name;
                style = new Notification.BigTextStyle()
                        .bigText(mUpdates.get(0).description);
                contentText = mUpdates.get(0).description;

                // Otherwise, use InboxStyle.
            } else {

                // Display the # of new updates as the title
                title = String.format("%d Hack The North update", mUpdates.size());
                title += mUpdates.size() > 1 ? "s" : "";

                Notification.InboxStyle inboxStyle = new Notification.InboxStyle()
                        .setBigContentTitle(title)
                        .setSummaryText("Hack The North");

                // Add the updates as strings underneath the title.
                int i = 0;
                for (Update update : mUpdates) {
                    if (i < 5) {
                        // Bold name with plain description text
                        String html = String.format("<b>%s</b> %s", update.name,
                                update.description);
                        inboxStyle.addLine(Html.fromHtml(html));
                        i++;
                    } else {
                        break;
                    }
                }

                style = inboxStyle;
                contentText = "Touch to view updates.";
            }

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, MainActivity.class), 0);
            Notification.Builder builder = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setStyle(style)
                    .setContentIntent(contentIntent);

            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            boolean vibrate = prefs.getBoolean(SettingsFragment.PREF_NOTIF_VIBRATE, true);
            if (vibrate) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }

            String sound = "content://settings/system/notification_sound";
            sound = prefs.getString(SettingsFragment.PREF_NOTIF_RINGTONE, sound);
            if (!"".equals(sound)) {
                notification.sound = Uri.parse(sound);
            }

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(UPDATES_NOTIFICATION_ID);
            notificationManager.notify(UPDATES_NOTIFICATION_ID, notification);

        }
    }
}
