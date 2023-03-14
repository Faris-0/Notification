package com.yuuna.notification;

import static com.yuuna.notification.MainActivity.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        new Task(pendingResult, intent, context).execute();
    }

    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final Intent intent;
        private final Context context;

        private Task(PendingResult pendingResult, Intent intent, Context context) {
            this.pendingResult = pendingResult;
            this.intent = intent;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            DBHandler dbHandler = new DBHandler(context);
            Integer sender_id = intent.getIntExtra("senderID", 0);
            if (intent.getAction().startsWith("READ_")) {
                ArrayList<MessageData> messageDataArrayList = new ArrayList<>(dbHandler.messageDataID(sender_id, false));
                for (int i = 0; i < messageDataArrayList.size(); i++) dbHandler.updateRead(messageDataArrayList.get(i).getId(), false);
                notification(context, messageDataArrayList, sender_id, true, false);
            } else {
                String time_now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("id")).format(new Date());
                dbHandler.addNewMessage(sender_id, "Me", String.valueOf(getMessageText(intent)), time_now, 0);
                ArrayList<MessageData> messageDataArrayList = new ArrayList<>(dbHandler.messageDataID(sender_id, true));
                for (int i = 0; i < messageDataArrayList.size(); i++) dbHandler.updateRead(messageDataArrayList.get(i).getId(), true);
                notification(context, messageDataArrayList, sender_id, false, true);
            }
            return null;
        }

        private CharSequence getMessageText(Intent intent) {
            Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
            if (remoteInput != null) return remoteInput.getCharSequence("key_text_reply");
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
