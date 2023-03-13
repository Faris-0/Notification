package com.yuuna.notification;

import static com.yuuna.notification.MainActivity.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.RemoteInput;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        Task asyncTask = new Task(pendingResult, intent, context);
        asyncTask.execute();
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final Context context;
        private ArrayList<MessageData> messageDataArrayList;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            //
            if (intent.getAction().startsWith("READ_")) {
                Integer id = intent.getIntExtra("ID", 0);
                SharedPreferences spData = context.getSharedPreferences("NOTIFY", Context.MODE_PRIVATE);
                spData.edit().remove("DATA"+id).apply();
                Type type = new TypeToken<ArrayList<MessageData>>() {}.getType();
                messageDataArrayList = new ArrayList<>(new Gson().fromJson(intent.getStringExtra("DATA"), type));
                ArrayList<MessageData> cacheMessageDataArrayList = new ArrayList<>();
                for (int i = 0; i < messageDataArrayList.size(); i++) {
                    cacheMessageDataArrayList.add(new MessageData(
                            messageDataArrayList.get(i).getId(),
                            messageDataArrayList.get(i).getName(),
                            messageDataArrayList.get(i).getMessage(),
                            messageDataArrayList.get(i).getDatetime(),
                            1));
                }
                spData.edit().putString("DATA"+id, new Gson().toJson(cacheMessageDataArrayList)).apply();
                //
                ArrayList<MessageData> newMessageDataArrayList = new Gson().fromJson(spData.getString("DATA", ""), new TypeToken<ArrayList<MessageData>>() {}.getType());
                ArrayList<Integer> integerArrayList = new ArrayList<>();
                for (int i = 0; i < newMessageDataArrayList.size(); i++) {
                    if (newMessageDataArrayList.get(i).getId() == id) {
                        integerArrayList.add(i);
                        newMessageDataArrayList.add(new MessageData(
                                newMessageDataArrayList.get(i).getId(),
                                newMessageDataArrayList.get(i).getName(),
                                newMessageDataArrayList.get(i).getMessage(),
                                newMessageDataArrayList.get(i).getDatetime(),
                                1));
                    }
                }
                for (int i = 0; i < integerArrayList.size(); i++) {
                    newMessageDataArrayList.remove(integerArrayList.get(i));
                }
                spData.edit().remove("DATA").apply();
                spData.edit().putString("DATA", new Gson().toJson(newMessageDataArrayList)).apply();
                notification(context, newMessageDataArrayList, id, true);
            }
            //
            StringBuilder sb = new StringBuilder();
            sb.append("Action: " + intent.getAction() + "\n");
            sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
            String log = sb.toString();
            Log.d("BroadcastReceiver", log);
            Log.d("BroadcastReceiver 1", String.valueOf(getMessageText(intent)));
            return log;
        }

        private CharSequence getMessageText(Intent intent) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) {
                return remoteInput.getCharSequence("key_text_reply");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
        }
    }
}
