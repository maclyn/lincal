package com.inipage.lincal.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.inipage.lincal.model.Record;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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

    public boolean addNewTask(String name, String icon, int color, String time, String daysOfWeek, int minutesForReminder, int productivityLevel){
        if(name.isEmpty() || icon.isEmpty()) return false;

        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.TASKS_NAME_COL_NAME, name);
        cv.put(DatabaseHelper.TASKS_ICON_COL_NAME, icon);
        cv.put(DatabaseHelper.TASKS_COLOR_COL_NAME, color);
        cv.put(DatabaseHelper.TASKS_REMINDER_TIME_COL_NAME, time);
        cv.put(DatabaseHelper.TASKS_REMINDER_DOW_COL_NAME, daysOfWeek.toLowerCase());
        cv.put(DatabaseHelper.TASKS_REMINDER_THRESHOLD_COL_NAME, minutesForReminder);
        cv.put(DatabaseHelper.TASKS_PRODUCTIVITY_COL_NAME, productivityLevel);
        cv.put(DatabaseHelper.TASKS_ARCHIVED_COL_NAME, 0);
        long result = db.insert(DatabaseHelper.TASKS_TABLE_NAME, null, cv);
        return result != -1L;
    }

    public boolean updateTask(long taskId,
                              String name, String icon, int color, String time, String daysOfWeek,
                              int minutesForReminder, int productivityLevel, int isArchived){
        ContentValues cv = new ContentValues();
        if(name != null)
            cv.put(DatabaseHelper.TASKS_NAME_COL_NAME, name);
        if(icon != null)
            cv.put(DatabaseHelper.TASKS_ICON_COL_NAME, icon);
        if(color != -1)
            cv.put(DatabaseHelper.TASKS_COLOR_COL_NAME, color);
        if(time != null)
            cv.put(DatabaseHelper.TASKS_REMINDER_TIME_COL_NAME, time);
        if(daysOfWeek != null)
            cv.put(DatabaseHelper.TASKS_REMINDER_DOW_COL_NAME, daysOfWeek.toLowerCase());
        if(minutesForReminder != -1)
            cv.put(DatabaseHelper.TASKS_REMINDER_THRESHOLD_COL_NAME, minutesForReminder);
        if(productivityLevel != -1)
            cv.put(DatabaseHelper.TASKS_PRODUCTIVITY_COL_NAME, productivityLevel);
        if(isArchived != -1)
            cv.put(DatabaseHelper.TASKS_ARCHIVED_COL_NAME, isArchived);

        long result = db.update(DatabaseHelper.TASKS_TABLE_NAME, cv,
                DatabaseHelper.TASKS_ID_COL_NAME + "=?", new String[] { String.valueOf(taskId)});
        return result != 0L;
    }

    public List<Task> getTasks(boolean includeArchived){
        Cursor c = null;
        if(includeArchived)
            c = db.query(DatabaseHelper.TASKS_TABLE_NAME, null, null, null, null, null, DatabaseHelper.TASKS_NAME_COL_NAME + " desc");
        else
            c = db.query(DatabaseHelper.TASKS_TABLE_NAME, null,
                    DatabaseHelper.TASKS_ARCHIVED_COL_NAME + "=?", new String[] { "0" },
                    null, null, DatabaseHelper.TASKS_NAME_COL_NAME + " desc");
        List<Task> tasks = new ArrayList<>();
        if(c.moveToFirst()){
            int idCol = c.getColumnIndex(DatabaseHelper.TASKS_ID_COL_NAME);
            int nameCol = c.getColumnIndex(DatabaseHelper.TASKS_NAME_COL_NAME);
            int iconCol = c.getColumnIndex(DatabaseHelper.TASKS_ICON_COL_NAME);
            int colorCol = c.getColumnIndex(DatabaseHelper.TASKS_COLOR_COL_NAME);
            int reminderTimeCol = c.getColumnIndex(DatabaseHelper.TASKS_REMINDER_TIME_COL_NAME);
            int reminderDowCol = c.getColumnIndex(DatabaseHelper.TASKS_REMINDER_DOW_COL_NAME);
            int reminderThresholdCol = c.getColumnIndex(DatabaseHelper.TASKS_REMINDER_THRESHOLD_COL_NAME);
            int productivityCol = c.getColumnIndex(DatabaseHelper.TASKS_PRODUCTIVITY_COL_NAME);
            int archivedCol = c.getColumnIndex(DatabaseHelper.TASKS_ARCHIVED_COL_NAME);

            while(!c.isAfterLast()){
                tasks.add(new Task(
                        c.getLong(idCol),
                        c.getString(nameCol),
                        c.getString(iconCol),
                        c.getInt(colorCol),
                        c.getString(reminderDowCol),
                        c.getString(reminderTimeCol),
                        c.getInt(reminderThresholdCol),
                        c.getInt(productivityCol),
                        c.getInt(archivedCol) == 1)
                );
                c.moveToNext();
            }
        }
        c.close();
        return tasks;
    }


    public List<TaskToday> getTasksWithTimeSpentToday(boolean includeArchived){
        List<Task> tasks = getTasks(includeArchived);
        List<TaskToday> todayTasks = new ArrayList<>();
        for(Task t : tasks){
            Cursor c = db.rawQuery("SELECT  * FROM records WHERE date(records.start_time) = date('now') AND records.task_id=?",
                    new String[]{ String.valueOf(t.getId())});
            int secondsForTask = 0;
            if(c.moveToFirst()){
                int secondsCol = c.getColumnIndex(DatabaseHelper.RECORDS_TOTAL_TIME_COL_NAME);
                while(!c.isAfterLast()){
                    secondsForTask += c.getInt(secondsCol);
                    c.moveToNext();
                }
            }
            c.close();

            todayTasks.add(new TaskToday(t, secondsForTask));
        }
        return todayTasks;
    }

    public boolean addNewRecord(long taskId, int totalTimeSeconds, String note, Date startTime, Date endTime){
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.RECORDS_TASK_ID_COL_NAME, taskId);
        cv.put(DatabaseHelper.RECORDS_NOTES_COL_NAME, note);
        cv.put(DatabaseHelper.RECORDS_TOTAL_TIME_COL_NAME, totalTimeSeconds);
        cv.put(DatabaseHelper.RECORDS_START_TIME_COL_NAME, DatabaseHelper.DB_RECORD_DATE_FORMAT.format(startTime));
        cv.put(DatabaseHelper.RECORDS_END_TIME_COL_NAME, DatabaseHelper.DB_RECORD_DATE_FORMAT.format(endTime));
        long result = db.insert(DatabaseHelper.RECORDS_TABLE_NAME, null, cv);
        return result != -1L;
    }

    /**
     * Get all records sorted by date. Useful for history browsing, though not efficient.
     * //TODO: Return a raw cursor for a {@link android.support.v4.widget.CursorAdapter}-esque thing.
     * @return The records.
     */
    public List<Record> getRecordsSortedByDay(){
        Cursor c = db.rawQuery("SELECT tasks.id AS task_id, records.id AS record_id, records.end_time, records.start_time, records.notes, records.total_time, tasks.name, tasks.color FROM records INNER JOIN tasks ON records.task_id = tasks.id ORDER BY datetime(records.start_time) ASC", new String[]{});
        List<Record> records = new ArrayList<>();
        if(c.moveToFirst()){
            int idCol = c.getColumnIndex("record_id");
            int notesCol = c.getColumnIndex(DatabaseHelper.RECORDS_NOTES_COL_NAME);
            int totalTimeCol = c.getColumnIndex(DatabaseHelper.RECORDS_TOTAL_TIME_COL_NAME);
            int startTimeCol = c.getColumnIndex(DatabaseHelper.RECORDS_START_TIME_COL_NAME);
            int endTimeCol = c.getColumnIndex(DatabaseHelper.RECORDS_END_TIME_COL_NAME);

            int taskIdCol = c.getColumnIndex("task_id");
            int taskColorCol = c.getColumnIndex(DatabaseHelper.TASKS_COLOR_COL_NAME);
            int taskNameCol = c.getColumnIndex(DatabaseHelper.TASKS_NAME_COL_NAME);

            while(!c.isAfterLast()){
                try {
                    records.add(new Record(
                            c.getLong(idCol),
                            c.getString(notesCol),
                            c.getInt(totalTimeCol),
                            DatabaseHelper.DB_RECORD_DATE_FORMAT.parse(c.getString(startTimeCol)),
                            DatabaseHelper.DB_RECORD_DATE_FORMAT.parse(c.getString(endTimeCol)),
                            c.getLong(taskIdCol),
                            c.getLong(taskColorCol),
                            c.getString(taskNameCol)
                    ));
                } catch (ParseException ignored) {
                }
                c.moveToNext();
            }
        }
        c.close();
        return records;
    }

    public Task getTask(long taskId) {
        List<Task> tasks = getTasks(true);
        for(Task t : tasks){ //Inefficient, but quick to write
            if(t.getId() == taskId) return t;
        }
        return null;
    }

    public void deleteTask(long taskId) {
        db.delete(DatabaseHelper.TASKS_TABLE_NAME, DatabaseHelper.TASKS_ID_COL_NAME + "=?", new String[] { String.valueOf(taskId) });
        db.delete(DatabaseHelper.RECORDS_TABLE_NAME, DatabaseHelper.RECORDS_TASK_ID_COL_NAME + "=?", new String[] { String.valueOf(taskId) });
    }

    public void editNoteForRecord(long recordId, String note) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.RECORDS_NOTES_COL_NAME, note);
        db.update(DatabaseHelper.RECORDS_TABLE_NAME,
                cv,
                DatabaseHelper.RECORDS_ID_COL_NAME + "=?",
                new String[] { String.valueOf(recordId) });
    }
}
