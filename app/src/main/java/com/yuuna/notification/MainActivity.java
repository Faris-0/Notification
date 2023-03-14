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

        tvDatetime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id")).format(new Date()));

        spNotify = getSharedPreferences("NOTIFY", Context.MODE_PRIVATE);

        loadData();

        findViewById(R.id.mSave).setOnClickListener(v -> {
            Integer id;
            if (etID.getText().toString().equals("")) id = 0; else id = Integer.valueOf(etID.getText().toString());
            dbHandler.addNewMessage(id, etName.getText().toString(), etMessage.getText().toString(), tvDatetime.getText().toString(), 0);
            loadData();
//            messageDataArrayList.add(new MessageData(
//                    id,
//                    etName.getText().toString(),
//                    etMessage.getText().toString(),
//                    tvDatetime.getText().toString(),
//                    0));
//            spNotify.edit().putString("CACHE_DATA", new Gson().toJson(messageDataArrayList)).apply();
//            loadData();
        });

        findViewById(R.id.mClear).setOnClickListener(v -> {
//            spNotify.edit().clear().apply();
            dbHandler.deleteAllMessage();
            loadData();
        });

        findViewById(R.id.mRefresh).setOnClickListener(v -> {
//            ArrayList<MessageData> combine = new ArrayList<>();
//            ArrayList<String> data = new ArrayList<>();
//            for (Map.Entry<String, ?> entry : spNotify.getAll().entrySet()) data.add(entry.getKey()+"_"+entry.getValue().toString());
//            for (int i = 0; i < data.size(); i++) {
//                if (data.get(i).startsWith("CACHE_DATA_")) {
//                    data.remove(i);
//                }
//            }
//            if (data.size() != 0) {
//                for (int i = 0; i < data.size(); i++) {
//                    spNotify.edit().putString(data.get(i).split("_")[0], new Gson().toJson(combine)).apply();
//                    combine.addAll(new Gson().fromJson(data.get(i).split("_")[1], new TypeToken<ArrayList<MessageData>>() {}.getType()));
//                }
//                if (combine.size() != 0) {
//                    spNotify.edit().clear().apply();
//                    spNotify.edit().putString("CACHE_DATA", new Gson().toJson(combine)).apply();
//                }
//            }
            loadData();
        });

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
            //
//            ArrayList<MessageData> combine = new ArrayList<>();
//            ArrayList<String> data = new ArrayList<>();
//            for (Map.Entry<String, ?> entry : spNotify.getAll().entrySet()) data.add(entry.getKey()+"_"+entry.getValue().toString());
//            for (int i = 0; i < data.size(); i++) {
//                if (!data.get(i).startsWith("CACHE_DATA_")) {
//                    combine.addAll(new Gson().fromJson(data.get(i).split("_")[1], new TypeToken<ArrayList<MessageData>>() {}.getType()));
//                }
//            }
//            spNotify.edit().putString("CACHE_DATA", new Gson().toJson(combine)).apply();
            //
        });
    }

    public static void loadData() {
        messageDataArrayList = new ArrayList<>(dbHandler.messageData());
        rvData.setLayoutManager(new LinearLayoutManager(context));
        rvData.setAdapter(new MessageAdater(messageDataArrayList, context));
//        // in below line we are getting data from gson
//        // and saving it to our array list
//        messageDataArrayList = new Gson().fromJson(spNotify.getString("CACHE_DATA", ""), new TypeToken<ArrayList<MessageData>>() {}.getType());
////        if (combine.toString().equals("[]")) {
////
////        } else {
////            messageDataArrayList = combine;
//////            spNotify.edit().remove("CACHE_DATA").apply();
//////            spNotify.edit().putString("CACHE_DATA", new Gson().toJson(messageDataArrayList)).apply();
//////            messageDataArrayList = new Gson().fromJson(spNotify.getString("CACHE_DATA", ""), new TypeToken<ArrayList<MessageData>>() {}.getType());
////        }
//
//        // checking below if the array list is empty or not
//        if (messageDataArrayList == null) messageDataArrayList = new ArrayList<>();
//
//        rvData.setLayoutManager(new LinearLayoutManager(context));
//        rvData.setAdapter(new MessageAdater(messageDataArrayList, context));
    }

    private void loadMessage(Integer sender_id) {
        ArrayList<MessageData> cacheMessageDataArrayList = new ArrayList<>(dbHandler.messageDataID(sender_id));
        Log.d("DATA", new Gson().toJson(cacheMessageDataArrayList));
        if (cacheMessageDataArrayList.size() != 0) notification(this, cacheMessageDataArrayList, sender_id, false);
//        if (messageData.size() != 0)
//        for (int i = 0; i < messageDataArrayList.size(); i++) {
//            if (messageDataArrayList.get(i).getId() == sender_id) {
//                cacheMessageDataArrayList.add(new MessageData(
//                        messageDataArrayList.get(i).getId(),
//                        messageDataArrayList.get(i).getName(),
//                        messageDataArrayList.get(i).getMessage(),
//                        messageDataArrayList.get(i).getDatetime(),
//                        messageDataArrayList.get(i).getRead()));
////                spNotify.edit().putString("DATA"+sender_id, new Gson().toJson(cacheMessageDataArrayList)).apply();
//            }
//        }
//        ArrayList<MessageData> messageData = new ArrayList<>(cacheMessageDataArrayList);
//        notification(this, messageData, sender_id, false);
        //
//        Type type = new TypeToken<ArrayList<MessageData>>() {}.getType();
//        ArrayList<MessageData> messageData = new ArrayList<>(new Gson().fromJson(spNotify.getString("DATA"+sender_id, ""), type));
//        notification(
//                this,
//                messageData,
//                sender_id,
//                false);
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
            Intent intent = new Intent(context, MainActivity.class)
                    .putExtra("senderID", sender_id)
                    .putExtra("isOPEN", true)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, sender_id, intent, 0);
            //--//
            Intent readIntent = new Intent(context, MyBroadcastReceiver.class).setAction("READ_")
                    .putExtra("senderID", sender_id)
                    .putExtra("DATA", new Gson().toJson(body));
            PendingIntent readPendingIntent = PendingIntent.getBroadcast(context, sender_id, readIntent, 0);
            //--//
            // Build a PendingIntent for the reply action to trigger.
            Intent replyIntent = new Intent(context, MyBroadcastReceiver.class).setAction("REPLY_")
                    .putExtra("senderID", sender_id)
                    .putExtra("DATA", new Gson().toJson(body));
            //
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