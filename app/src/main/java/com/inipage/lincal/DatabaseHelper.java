package com.inipage.lincal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String FILENAME = "database.db";
    public static final int VERSION = 3;

    public static final String TASKS_TABLE_NAME = "tasks";
    public static final String TASKS_ID_COL_NAME = "id";
    public static final String TASKS_NAME_COL_NAME = "name";
    public static final String TASKS_ICON_COL_NAME = "icon";
    public static final String TASKS_COLOR_COL_NAME = "color";
    public static final String TASKS_REMINDER_TIME_COL_NAME = "reminder_time";
    public static final String TASKS_REMINDER_THRESHOLD_COL_NAME = "reminder_threshold";
    public static final String TASKS_REMINDER_DOW_COL_NAME = "reminder_dow";
    public static final String TASKS_CUSTOMER_ID_COL_NAME = "customer_id";
    private static final String CREATE_TABLE_TASKS = "CREATE TABLE \"" + TASKS_TABLE_NAME + "\" (\n" +
            "\t`" + TASKS_ID_COL_NAME + "`\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
            "\t`" + TASKS_NAME_COL_NAME + "`\tTEXT NOT NULL UNIQUE,\n" +
            "\t`" + TASKS_ICON_COL_NAME + "`\tTEXT NOT NULL,\n" +
            "\t`" + TASKS_REMINDER_DOW_COL_NAME + "`\tTEXT,\n" +
            "\t`" + TASKS_COLOR_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + TASKS_REMINDER_TIME_COL_NAME + "`\tTEXT NOT NULL,\n" +
            "\t`" + TASKS_CUSTOMER_ID_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + TASKS_REMINDER_THRESHOLD_COL_NAME + "`\tINTEGER\n" +
            ")";

    public static final String RECORDS_TABLE_NAME = "records";
    public static final String RECORDS_ID_COL_NAME = "id";
    public static final String RECORDS_TASK_ID_COL_NAME = "task_id";
    public static final String RECORDS_START_TIME_COL_NAME = "start_time";
    public static final String RECORDS_END_TIME_COL_NAME = "end_time";
    public static final String RECORDS_TOTAL_TIME_COL_NAME = "total_time";
    public static final String RECORDS_NOTES_COL_NAME = "notes";
    public static final String RECORDS_BILLED_COL_NAME = "billed";
    private static final String CREATE_TABLE_RECORDS = "CREATE TABLE `" + RECORDS_TABLE_NAME + "` (\n" +
            "\t`" + RECORDS_ID_COL_NAME + "`\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
            "\t`" + RECORDS_TASK_ID_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + RECORDS_START_TIME_COL_NAME + "`\tTEXT NOT NULL UNIQUE,\n" +
            "\t`" + RECORDS_END_TIME_COL_NAME + "`\tTEXT NOT NULL UNIQUE,\n" +
            "\t`" + RECORDS_TOTAL_TIME_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + RECORDS_NOTES_COL_NAME + "`\tTEXT,\n" +
            "\t`" + RECORDS_BILLED_COL_NAME +"`\tINTEGER NOT NULL,\n" +
            "\tFOREIGN KEY(`" + RECORDS_TASK_ID_COL_NAME + "`) REFERENCES tasks(id)\n" +
            ")";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_TASKS);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECORDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //What could go wrong?
        sqLiteDatabase.execSQL("DROP TABLE " + RECORDS_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE " + TASKS_TABLE_NAME);
        sqLiteDatabase.execSQL(CREATE_TABLE_TASKS);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECORDS);
    }
}
