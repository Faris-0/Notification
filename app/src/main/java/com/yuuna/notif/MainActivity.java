package com.yuuna.notif;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

public class MainActivity extends Activity {

    private String CHANNEL_ID = "CHANNEL_ID";
    private Boolean doubleBackToExit = false;

    // Key for the string that's delivered in the action's intent.
    private static final String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.mNotif).setOnClickListener(v -> {
            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            //--//
            Intent readIntent = new Intent(this, MyBroadcastReceiver.class).setAction("READ").putExtra("ID", 22);
            PendingIntent readPendingIntent = PendingIntent.getBroadcast(this, 0, readIntent, 0);
            //--//
            // Build a PendingIntent for the reply action to trigger.
            Intent replyIntent = new Intent(this, MyBroadcastReceiver.class).setAction("REPLY").putExtra("ID", 22);
            //
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Create the reply action and add the remote input.
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, "REPLY", replyPendingIntent)
                            .addRemoteInput(new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel("REPLY").build()).build();
            //--//
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setWhen(System.currentTimeMillis())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    // Set one alert with same id
                    .setOnlyAlertOnce(true)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setStyle(new NotificationCompat.MessagingStyle("Me")
                            .setConversationTitle("Team lunch")
                            .addMessage("Hi", System.currentTimeMillis()/1000, "ME") // Pass in null for user.
                            .addMessage("What's up?", System.currentTimeMillis()/1000, "Coworker")
                            .addMessage("Not much", System.currentTimeMillis()/1000, "ME")
                            .addMessage("How about lunch?", System.currentTimeMillis()/1000, "Coworker"))
                    .addAction(action)
                    .addAction(0, "READ", readPendingIntent);
            //--//
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }
            //--//
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManagerCompat.from(this).notify(22, builder.build());
            //--//
        });
    }

    public static void sendNotif(Context context, String message, Integer id) {
        String CHANNEL_ID = "CHANNEL_ID";
        //--//
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        //-//
        // Build a PendingIntent for the reply action to trigger.
        Intent replyIntent = new Intent(context, MyBroadcastReceiver.class).setAction("REPLY").putExtra("ID", 22);
        //
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Create the reply action and add the remote input.
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, "REPLY", replyPendingIntent)
                .addRemoteInput(new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel("REPLY").build()).build();
        //--//
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                // Set one alert with same id
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setStyle(new NotificationCompat.MessagingStyle("Me")
                        .setConversationTitle("Team lunch")
                        .addMessage("Hi", System.currentTimeMillis()/1000, "ME") // Pass in null for user.
                        .addMessage("What's up?", System.currentTimeMillis()/1000 + 1, "Coworker")
                        .addMessage("Not much", System.currentTimeMillis()/1000 + 2, "ME")
                        .addMessage("How about lunch?", System.currentTimeMillis()/1000 + 3, "Coworker")
                        .addMessage(message, System.currentTimeMillis()/1000 + 4, "ME"))
                .addAction(action);
        //--//
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        //--//
        // notificationId is a unique int for each notification that you must define
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManagerCompat.from(context).notify(id, builder.build());
        //--//
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExit) {
            finishAndRemoveTask();
        }

        doubleBackToExit = true;
        Toast.makeText(this, "Tekan sekali lagi untuk keluar dari aplikasi", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExit = false;
            }
        }, 2000);
    }
}