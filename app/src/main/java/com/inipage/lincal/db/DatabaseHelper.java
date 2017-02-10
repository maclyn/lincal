package com.inipage.lincal.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String FILENAME = "database.db";
    public static final int VERSION = 7;

    public static final SimpleDateFormat DB_REMINDER_TIME_FORMAT = new SimpleDateFormat("h:mm aa", Locale.US);
    public static final SimpleDateFormat DB_RECORD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final String[] DB_DOW_ENTRIES = new String[] { "su", "mo", "tu", "we", "th", "fr",
            "sa" };
    public static final int[] DB_DOW_CAL_FIELDS = new int[] { Calendar.SUNDAY, Calendar.MONDAY,
            Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
            Calendar.SATURDAY};

    public static final String TASKS_TABLE_NAME = "tasks";
    public static final String TASKS_ID_COL_NAME = "id";
    public static final String TASKS_NAME_COL_NAME = "name";
    public static final String TASKS_ICON_COL_NAME = "icon";
    public static final String TASKS_COLOR_COL_NAME = "color";
    public static final String TASKS_REMINDER_TIME_COL_NAME = "reminder_time";
    public static final String TASKS_REMINDER_THRESHOLD_COL_NAME = "reminder_threshold";
    public static final String TASKS_REMINDER_DOW_COL_NAME = "reminder_dow";
    public static final String TASKS_ARCHIVED_COL_NAME = "archived";
    public static final String TASKS_PRODUCTIVITY_COL_NAME = "productivity";
    private static final String CREATE_TABLE_TASKS = "CREATE TABLE \"" + TASKS_TABLE_NAME + "\" (\n" +
            "\t`" + TASKS_ID_COL_NAME + "`\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
            "\t`" + TASKS_NAME_COL_NAME + "`\tTEXT NOT NULL UNIQUE,\n" +
            "\t`" + TASKS_ICON_COL_NAME + "`\tTEXT NOT NULL,\n" +
            "\t`" + TASKS_REMINDER_DOW_COL_NAME + "`\tTEXT,\n" +
            "\t`" + TASKS_COLOR_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + TASKS_REMINDER_TIME_COL_NAME + "`\tTEXT NOT NULL,\n" +
            "\t`" + TASKS_REMINDER_THRESHOLD_COL_NAME + "`\tINTEGER,\n" +
            "\t`" + TASKS_PRODUCTIVITY_COL_NAME + "`\tINTEGER NOT NULL DEFAULT 0,\n" +
            "\t`" + TASKS_ARCHIVED_COL_NAME + "`\tINTEGER NOT NULL DEFAULT 0,\n" +
            ")";

    public static final String RECORDS_TABLE_NAME = "records";
    public static final String RECORDS_ID_COL_NAME = "id";
    public static final String RECORDS_TASK_ID_COL_NAME = "task_id";
    public static final String RECORDS_START_TIME_COL_NAME = "start_time";
    public static final String RECORDS_END_TIME_COL_NAME = "end_time";
    public static final String RECORDS_TOTAL_TIME_COL_NAME = "total_time";
    public static final String RECORDS_NOTES_COL_NAME = "notes";
    private static final String CREATE_TABLE_RECORDS = "CREATE TABLE `" + RECORDS_TABLE_NAME + "` (\n" +
            "\t`" + RECORDS_ID_COL_NAME + "`\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
            "\t`" + RECORDS_TASK_ID_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + RECORDS_START_TIME_COL_NAME + "`\tTEXT NOT NULL UNIQUE,\n" +
            "\t`" + RECORDS_END_TIME_COL_NAME + "`\tTEXT NOT NULL UNIQUE,\n" +
            "\t`" + RECORDS_TOTAL_TIME_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + RECORDS_NOTES_COL_NAME + "`\tTEXT,\n" +
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
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //What could go wrong?
        if(oldVersion < 5){ //Full database rebuild
            sqLiteDatabase.execSQL("DROP TABLE " + RECORDS_TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE " + TASKS_TABLE_NAME);
            sqLiteDatabase.execSQL(CREATE_TABLE_TASKS);
            sqLiteDatabase.execSQL(CREATE_TABLE_RECORDS);
            return;
        }

        if(oldVersion == 5){ //5 --> 7
            sqLiteDatabase.execSQL("ALTER TABLE `tasks` ADD COLUMN `productivity` INTEGER NOT NULL DEFAULT 0");
            sqLiteDatabase.execSQL("ALTER TABLE `tasks` ADD COLUMN `archived` INTEGER NOT NULL DEFAULT 0");
        }

        if(oldVersion == 6){
            sqLiteDatabase.execSQL("ALTER TABLE `tasks` ADD COLUMN `archived` INTEGER NOT NULL DEFAULT 0");
        }
    }
}