package com.yuuna.notification;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {

    private EditText etID, etName, etMessage;
    private TextView tvDatetime;
    public static RecyclerView rvData;

    public static SharedPreferences spNotify;
    public static Context context;
    public static DBHandler dbHandler;

    public static ArrayList<MessageData> messageDataArrayList;

    private String CHANNEL_ID = "CHANNEL_ID";
    private Boolean doubleBackToExit = false;

    // Key for the string that's delivered in the action's intent.
    private static final String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etID = findViewById(R.id.mID);
        etName = findViewById(R.id.mName);
        etMessage = findViewById(R.id.mMessage);
        tvDatetime = findViewById(R.id.mDate);
        rvData = findViewById(R.id.mData);

        context = this;
        dbHandler = new DBHandler(this);
        spNotify = getSharedPreferences("NOTIFY", Context.MODE_PRIVATE);

        tvDatetime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id")).format(new Date()));

        loadData();

        findViewById(R.id.mSave).setOnClickListener(v -> {
            Integer sender_id = 0;
            if (!etID.getText().toString().equals("")) sender_id = Integer.valueOf(etID.getText().toString());
            dbHandler.addNewMessage(sender_id, etName.getText().toString(), etMessage.getText().toString(), tvDatetime.getText().toString(), 0);
            loadData();
        });
        findViewById(R.id.mClear).setOnClickListener(v -> {
            dbHandler.deleteAllMessage();
            loadData();
        });
        findViewById(R.id.mRefresh).setOnClickListener(v -> loadData());
        findViewById(R.id.mShow).setOnClickListener(v -> {
            ArrayList<Integer> integerArrayList = new ArrayList<>();
            for (int i = 0; i < messageDataArrayList.size(); i++) {
                integerArrayList.add(messageDataArrayList.get(i).getSender_id());
            }
            HashSet hs = new HashSet();
            hs.addAll(integerArrayList);
            integerArrayList.clear();
            integerArrayList.addAll(hs);
            for (int i = 0; i < integerArrayList.size(); i++) {
                loadMessage(integerArrayList.get(i));
            }
            loadData();
        });
    }

    public static void loadData() {
        messageDataArrayList = new ArrayList<>(dbHandler.messageData());
        rvData.setLayoutManager(new LinearLayoutManager(context));
        rvData.setAdapter(new MessageAdater(messageDataArrayList, context));
    }

    private void loadMessage(Integer sender_id) {
        ArrayList<MessageData> cacheMessageDataArrayList = new ArrayList<>(dbHandler.messageDataID(sender_id));
        if (cacheMessageDataArrayList.size() != 0) notification(this, cacheMessageDataArrayList, sender_id, false);
    }

    public static void notification(Context context, ArrayList<MessageData> body, Integer sender_id, Boolean isRemove) {
        if (isRemove) {
            //--//
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManagerCompat.from(context).cancel(sender_id);
            //--//
        } else {
            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(context, MainActivity.class).putExtra("senderID", sender_id).putExtra("isOPEN", true)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, sender_id, intent, 0);
            //--//
            Intent readIntent = new Intent(context, MyBroadcastReceiver.class).setAction("READ_").putExtra("senderID", sender_id);
            PendingIntent readPendingIntent = PendingIntent.getBroadcast(context, sender_id, readIntent, 0);
            //--//
            // Build a PendingIntent for the reply action to trigger.
            Intent replyIntent = new Intent(context, MyBroadcastReceiver.class).setAction("REPLY_").putExtra("senderID", sender_id);
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, sender_id, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Create the reply action and add the remote input.
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, "Reply", replyPendingIntent)
                    .addRemoteInput(new RemoteInput.Builder("key_text_reply").setLabel("Reply").build()).build();
            //--//
            NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle("ME");
            Date date = null;
            for (int i = 0; i < body.size(); i++) {
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id")).parse(body.get(i).getDatetime());
                    messagingStyle.addMessage(body.get(i).getMessage(), date.getTime(), body.get(i).getName());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            //--//
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setWhen(date.getTime())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    // Set one alert with same id
                    .setOnlyAlertOnce(true)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setStyle(messagingStyle)
                    .setColor(Color.parseColor("#FFBB86FC"))
                    .addAction(action)
                    .addAction(0, "Mark as read", readPendingIntent);
            //--//
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }
            //--//
            // notificationId is a unique int for each notification that you must define
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
            NotificationManagerCompat.from(context).notify(sender_id, builder.build());
            //--//
        }
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