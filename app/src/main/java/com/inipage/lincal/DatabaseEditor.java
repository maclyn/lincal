package com.inipage.lincal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class DatabaseEditor {
    private static DatabaseEditor instance;

    private SQLiteDatabase db;

    public static DatabaseEditor getInstance(Context context){
        return (instance == null ? (instance = new DatabaseEditor(context)) : instance);
    }

    public DatabaseEditor(Context context){
        db = new DatabaseHelper(context, DatabaseHelper.FILENAME, null, DatabaseHelper.VERSION).getWritableDatabase();
    }

    public boolean addNewTask(String name, String icon, int color, String time, String daysOfWeek, int minutesForReminder){
        if(name.isEmpty() || icon.isEmpty()) return false;

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TASKS_NAME_COL_NAME, name);
        cv.put(DatabaseHelper.TASKS_ICON_COL_NAME, icon);
        cv.put(DatabaseHelper.TASKS_COLOR_COL_NAME, color);
        cv.put(DatabaseHelper.TASKS_REMINDER_TIME_COL_NAME, time);
        cv.put(DatabaseHelper.TASKS_REMINDER_DOW_COL_NAME, daysOfWeek.toLowerCase());
        cv.put(DatabaseHelper.TASKS_REMINDER_THRESHOLD_COL_NAME, minutesForReminder);
        cv.put(DatabaseHelper.TASKS_CUSTOMER_ID_COL_NAME, -1);
        long result = db.insert(DatabaseHelper.TASKS_TABLE_NAME, null, cv);
        return result != -1L;
    }

    public List<Task> getTasks(){
        Cursor c = db.query(DatabaseHelper.TASKS_TABLE_NAME, null, null, null, null, null, DatabaseHelper.TASKS_NAME_COL_NAME + " desc");
        List<Task> tasks = new ArrayList<>();
        if(c.moveToFirst()){
            int idCol = c.getColumnIndex(DatabaseHelper.TASKS_ID_COL_NAME);
            int nameCol = c.getColumnIndex(DatabaseHelper.TASKS_NAME_COL_NAME);
            int iconCol = c.getColumnIndex(DatabaseHelper.TASKS_ICON_COL_NAME);
            int colorCol = c.getColumnIndex(DatabaseHelper.TASKS_COLOR_COL_NAME);
            int reminderTimeCol = c.getColumnIndex(DatabaseHelper.TASKS_REMINDER_TIME_COL_NAME);
            int reminderDowCol = c.getColumnIndex(DatabaseHelper.TASKS_REMINDER_DOW_COL_NAME);
            int reminderThresholdCol = c.getColumnIndex(DatabaseHelper.TASKS_REMINDER_THRESHOLD_COL_NAME);
            int reminderCustIdCol = c.getColumnIndex(DatabaseHelper.TASKS_CUSTOMER_ID_COL_NAME);

            while(!c.isAfterLast()){
                tasks.add(new Task(
                        c.getLong(idCol),
                        c.getString(nameCol),
                        c.getString(iconCol),
                        c.getInt(colorCol),
                        c.getString(reminderDowCol),
                        c.getString(reminderTimeCol),
                        c.getInt(reminderThresholdCol),
                        c.getLong(reminderCustIdCol)
                ));
                c.moveToNext();
            }
        }
        c.close();
        return tasks;
    }
}
