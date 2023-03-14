package com.yuuna.notification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "notify";
    private static final Integer DB_VERSION = 1;
    private static final String TABLE_NAME = "notification";
    private static final String ID_COL = "id";
    private static final String SENDERID_COL = "sender_id";
    private static final String NAME_COL = "name";
    private static final String MESSAGE_COL = "message";
    private static final String DATETIME_COL = "datetime";
    private static final String READ_COL = "read";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SENDERID_COL + " INTEGER, " +
                NAME_COL + " TEXT, " +
                MESSAGE_COL + " TEXT, " +
                DATETIME_COL + " TEXT, " +
                READ_COL + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addNewMessage(Integer sender_id, String name, String message, String datetime, Integer read) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SENDERID_COL, sender_id);
        values.put(NAME_COL, name);
        values.put(MESSAGE_COL, message);
        values.put(DATETIME_COL, datetime);
        values.put(READ_COL, read);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateRead(Integer id, Boolean isMe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(READ_COL, 1);

        if (isMe) db.update(TABLE_NAME, values, "id=? AND name!='Me'", new String[]{String.valueOf(id)});
        else db.update(TABLE_NAME, values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllMessage() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public ArrayList<MessageData> messageDataID(Integer sender_id, Boolean isMe)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursorMessage;
        if (isMe) cursorMessage = db.rawQuery("SELECT * FROM notification WHERE sender_id=" + sender_id, null);
        else cursorMessage = db.rawQuery("SELECT * FROM notification WHERE name!='Me' AND sender_id=" + sender_id + " AND read=0", null);

        ArrayList<MessageData> messageDataArrayList = new ArrayList<>();
        if (cursorMessage.moveToFirst()) {
            do {
                messageDataArrayList.add(new MessageData(
                        cursorMessage.getInt(0),
                        cursorMessage.getInt(1),
                        cursorMessage.getString(2),
                        cursorMessage.getString(3),
                        cursorMessage.getString(4),
                        cursorMessage.getInt(5))
                );
            } while (cursorMessage.moveToNext());
        }
        cursorMessage.close();
        return messageDataArrayList;
    }

    public ArrayList<MessageData> messageData()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursorMessage = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        ArrayList<MessageData> messageDataArrayList = new ArrayList<>();
        if (cursorMessage.moveToFirst()) {
            do {
                messageDataArrayList.add(new MessageData(
                        cursorMessage.getInt(0),
                        cursorMessage.getInt(1),
                        cursorMessage.getString(2),
                        cursorMessage.getString(3),
                        cursorMessage.getString(4),
                        cursorMessage.getInt(5))
                );
            } while (cursorMessage.moveToNext());
        }
        cursorMessage.close();
        return messageDataArrayList;
    }
}
