package com.inipage.lincal.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String FILENAME = "database.db";
    public static final int VERSION = 10;

    public static final SimpleDateFormat DB_REMINDER_TIME_FORMAT = new SimpleDateFormat("h:mm aa", Locale.US);
    public static final SimpleDateFormat DB_RECORD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final String[] DB_DOW_ENTRIES = new String[] { "su", "mo", "tu", "we", "th", "fr",
            "sa" };
    public static final int[] DB_DOW_CAL_FIELDS = new int[] {
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
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
            "\t`" + TASKS_ARCHIVED_COL_NAME + "`\tINTEGER NOT NULL DEFAULT 0\n" +
            ")";

    public static final String TODOS_TABLE_NAME = "todos";
    public static final String TODOS_ID_COL_NAME = "id";
    public static final String TODOS_TITLE_COL_NAME = "title";
    public static final String TODOS_TASK_ID_COL_NAME = "task_id";
    public static final String TODOS_DUE_DATE_COL_NAME = "due_date";
    public static final String TODOS_IMPORTANCE_COL_NAME = "importance";
    public static final String TODOS_COMPLETE_COL_NAME = "complete";
    private static final String CREATE_TABLE_TODOS = "CREATE TABLE `" + TODOS_TABLE_NAME + "` (\n" +
            "\t`" + TODOS_ID_COL_NAME + "`\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
            "\t`" + TODOS_TITLE_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + TODOS_TASK_ID_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + TODOS_DUE_DATE_COL_NAME + "`\tTEXT NOT NULL,\n" +
            "\t`" + TODOS_IMPORTANCE_COL_NAME + "`\tINTEGER NOT NULL DEFAULT 50,\n" +
            "\t`" + TODOS_COMPLETE_COL_NAME + "`\tINTEGER NOT NULL DEFAULT 0,\n" +
            "\tFOREIGN KEY(`" + TODOS_TASK_ID_COL_NAME + "`) REFERENCES tasks(id)\n" +
            ")";

    public static final String RECORDS_TABLE_NAME = "records";
    public static final String RECORDS_ID_COL_NAME = "id";
    public static final String RECORDS_TASK_ID_COL_NAME = "task_id";
    public static final String RECORDS_START_TIME_COL_NAME = "start_time";
    public static final String RECORDS_END_TIME_COL_NAME = "end_time";
    public static final String RECORDS_TOTAL_TIME_COL_NAME = "total_time";
    public static final String RECORDS_NOTES_COL_NAME = "notes";
    public static final String RECORDS_TODO_ID_COL_NAME = "todo_id";
    private static final String CREATE_TABLE_RECORDS = "CREATE TABLE `" + RECORDS_TABLE_NAME + "` (\n" +
            "\t`" + RECORDS_ID_COL_NAME + "`\tINTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,\n" +
            "\t`" + RECORDS_TASK_ID_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + RECORDS_START_TIME_COL_NAME + "`\tTEXT NOT NULL,\n" +
            "\t`" + RECORDS_END_TIME_COL_NAME + "`\tTEXT NOT NULL,\n" +
            "\t`" + RECORDS_TOTAL_TIME_COL_NAME + "`\tINTEGER NOT NULL,\n" +
            "\t`" + RECORDS_NOTES_COL_NAME + "`\tTEXT,\n" +
            "\t`" + RECORDS_TODO_ID_COL_NAME + "`\tINTEGER NOT NULL DEFAULT -1,\n" +
            "\tFOREIGN KEY(`" + RECORDS_TASK_ID_COL_NAME + "`) REFERENCES tasks(id),\n" +
            "\tFOREIGN KEY(`" + RECORDS_TODO_ID_COL_NAME + "`) REFERENCES todos(id)\n" +
            ")";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_TASKS);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECORDS);
        sqLiteDatabase.execSQL(CREATE_TABLE_TODOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(oldVersion < 10){ //Full database rebuild
            try {
                sqLiteDatabase.execSQL("DROP TABLE " + RECORDS_TABLE_NAME); //v1
                sqLiteDatabase.execSQL("DROP TABLE " + TASKS_TABLE_NAME); //v1\
                sqLiteDatabase.execSQL("DROP TABLE " + TODOS_TABLE_NAME);
            } catch (Exception ignored) {} //Will fail to drop later ones; not bad so long as DROPs sequential by table date though
            sqLiteDatabase.execSQL(CREATE_TABLE_TASKS);
            sqLiteDatabase.execSQL(CREATE_TABLE_TODOS);
            sqLiteDatabase.execSQL(CREATE_TABLE_RECORDS);
            return;
        }
    }
}
