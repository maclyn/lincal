package com.inipage.lincal.background;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;

import com.inipage.lincal.MainActivity;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.db.DatabaseHelper;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class ReminderService extends Service {
    public static final String TAG = "ReminderService";

    public static final String ACTION_UPDATE_REMINDERS = "update_reminders";
    public static final String ACTION_DISPLAY_REMINDER = "display_reminder";

    public static final String EXTRA_REMINDERS_TO_DISPLAY_LONG_ARRAY = "reminder_to_display";

    private static final int REMINDER_SERVICE_PI_ID = 701;
    private static final int SHOW_ACTIVITY_PI_ID = 702;

    private static final int BASE_NOTIFICATION_ID = 1500;

    public ReminderService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() == null || intent.getAction().isEmpty()) return START_NOT_STICKY;

        if(intent.getAction().equals(ACTION_UPDATE_REMINDERS)) {
            //Unoptimized first go at finding closest next date
            List<Task> allTasks = DatabaseEditor.getInstance(this).getTasks(false);
            List<Pair<Date, Long>> allReminders = new ArrayList<>(allTasks.size() * 4); //This be rough
            Calendar today = new GregorianCalendar();
            for (Task t : allTasks) {
                if (!t.hasReminder()) continue;

                Date time = t.getReminderTimeAsDate();
                String[] daysOfWeek = t.getReminderDow().split(" "); //RIP
                Calendar cal = (Calendar) today.clone();
                cal.set(Calendar.HOUR_OF_DAY, time.getHours());
                cal.set(Calendar.MINUTE, time.getMinutes());
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                for (String dow : daysOfWeek) {
                    for (int i = 0; i < DatabaseHelper.DB_DOW_ENTRIES.length; i++) {
                        if (dow.toLowerCase(Locale.US).equals(DatabaseHelper.DB_DOW_ENTRIES[i])) {
                            cal.set(Calendar.DAY_OF_WEEK, DatabaseHelper.DB_DOW_CAL_FIELDS[i]);
                            if (cal.before(today)) {
                                cal.roll(Calendar.WEEK_OF_YEAR, 1);
                                allReminders.add(new Pair<>(cal.getTime(), t.getId()));
                                cal.roll(Calendar.WEEK_OF_YEAR, -1);
                            } else {
                                allReminders.add(new Pair<>(cal.getTime(), t.getId()));
                            }
                            break;
                        }
                    }
                }
            }

            Log.d(TAG, "Reminders found: " + allReminders.size());
            for(Pair<Date, Long> reminder : allReminders){
                Log.d(TAG, "Reminder: " + reminder.first + ", " + reminder.second);
            }

            List<Pair<Date, Long>> nearestReminders = new ArrayList<>();
            for (Pair<Date, Long> reminder : allReminders) {
                if (nearestReminders.isEmpty()) {
                    nearestReminders.add(reminder);
                    continue;
                }

                if (reminder.first.before(nearestReminders.get(0).first)) {
                    nearestReminders.clear();
                    nearestReminders.add(reminder);
                } else if (reminder.first.equals(nearestReminders.get(0).first)) {
                    nearestReminders.add(reminder);
                }
            }

            if(!nearestReminders.isEmpty()) {
                long[] taskIdList = new long[nearestReminders.size()];
                for(int i = 0; i < nearestReminders.size(); i++){
                    Pair<Date, Long> reminder = nearestReminders.get(i);
                    taskIdList[i] = reminder.second;
                }
                Intent displayReminderIntent = new Intent(this, ReminderService.class);
                displayReminderIntent.setAction(ACTION_DISPLAY_REMINDER);
                displayReminderIntent.putExtra(EXTRA_REMINDERS_TO_DISPLAY_LONG_ARRAY, taskIdList);

                PendingIntent pi = PendingIntent.getService(this, REMINDER_SERVICE_PI_ID,
                        displayReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

                am.cancel(pi);
                am.setExact(AlarmManager.RTC_WAKEUP, nearestReminders.get(0).first.getTime(), pi);
            }
        } else if (intent.getAction().equals(ACTION_DISPLAY_REMINDER)) {
            Log.d(TAG, "Displaying reminder!");

            long[] taskIds = intent.getLongArrayExtra(EXTRA_REMINDERS_TO_DISPLAY_LONG_ARRAY);
            Log.d(TAG, "IDs: ");
            for(long taskId : taskIds){
                Log.d(TAG, "---Task ID: " + taskId);
            }

            //Notify for reminder; icon is the icon of the thing
            List<TaskToday> tasksWithTimeSpent = DatabaseEditor.getInstance(this).getTasksWithRemindersAndTimeSpentToday(false);
            //O(n^2) sadness, but not too bad (usually just O(n) -- tasksToNotifyFor is like 1 normally
            List<TaskToday> toNotifyFor = new ArrayList<>();
            for(TaskToday task : tasksWithTimeSpent){
                for(long id : taskIds){
                    if(id == task.getId()){
                        if(task.getSecondsSoFar() < (task.getReminderThreshold() * 60)){
                            toNotifyFor.add(task);
                        }
                    }
                }
            }

            //Notify!
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            for(TaskToday task : toNotifyFor) {
                int minutesLeft = (int) Math.floor( ((task.getReminderThreshold() * 60) - task.getSecondsSoFar()) / 60 );
                String msg = "Only" + (minutesLeft > 1 ? minutesLeft + " minutes " : (minutesLeft > 0 ? " one minute " : " a few seconds ")) + "left!";

                NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                        .setContentTitle("Do some '" + task.getName() + "'")
                        .setContentText(msg)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setSmallIcon(getResources().getIdentifier(task.getIcon(), "drawable", getPackageName()))
                        .setOngoing(false)
                        .setContentIntent(PendingIntent.getActivity(this, SHOW_ACTIVITY_PI_ID, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
                nm.notify((int) (BASE_NOTIFICATION_ID + task.getId()), nBuilder.build());
            }

            Intent scheduleReminders = new Intent(this, ReminderService.class);
            scheduleReminders.setAction(ReminderService.ACTION_UPDATE_REMINDERS);
            startService(scheduleReminders);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
