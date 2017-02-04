package com.inipage.lincal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;

import java.sql.Time;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    /** ACTIONs and EXTRAs to instrument this service. **/

    public static final String ACTION_START_TIMER = "start_timer";
    public static final String ACTION_STOP_TIMER = "stop_timer";
    public static final String ACTION_START_POMODORO = "start_pomdoro";
    public static final String ACTION_START_BREAK = "start_break";

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_ADDED_SECONDS = "added_seconds";

    /** Broadcast to alert TaskToday of state changes. **/

    public static final String BROADCAST_TIMER_STOPPED = "timer_stopped";
    public static final String BROADCAST_TIMER_STARTED = "timer_started";

    private static final int ONGOING_NID = 500;

    private static final int SHOW_ACTIVITY_PI_ID = 1000;
    private static final int STOP_TIMER_PI_ID = 1001;
    private static final int RESUME_TIMER_PI_ID = 1002;
    private static final int BREAK_TIMER_PI_ID = 1003;

    /** Internal service semantics. **/
    private static final long POMODORO_DURATION = 1 * 60 * 1000; //25 minutes
    private static final long BREAK_DURATION = 1 * 60 * 1000; //5 minutes

    private static final int STATE_DEAD = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_POMODORO = 2;
    private static final int STATE_P_BREAK = 3;

    private int mState = STATE_DEAD;
    public static long mTaskId = -1;
    private long mCachedTaskId = -1; //When saving state for post-break resume
    private long mStartTime = -1L;
    private Task mTask = null;
    private int mTaskDrawableId = -1;
    private Timer mTimer = new Timer();

    public TimerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(action == null){
            stopSelf();
            return START_NOT_STICKY;
        }

        switch(action){
            case ACTION_START_TIMER:
            case ACTION_START_POMODORO:
                //Depending on what's happening
                if(mState != STATE_DEAD && mState != STATE_P_BREAK) {
                    manageTimerStop();
                }

                //Start a timer for a given taskId
                mTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
                if(mTaskId == -1){ stopSelf(); return START_NOT_STICKY; }
                mTask = DatabaseEditor.getInstance(this).getTask(mTaskId);
                mTaskDrawableId = getResources().getIdentifier(mTask.getIcon(), "drawable", getPackageName());

                TimerTask updateTimer = new TimerTask() {
                    @Override
                    public void run() {
                        timerUpdate();
                    }
                };

                mStartTime = System.currentTimeMillis();
                mTimer.scheduleAtFixedRate(updateTimer, new Date(), 1000L);

                mState = action.equals(ACTION_START_TIMER) ? STATE_RUNNING : STATE_POMODORO;

                Intent startIntent = new Intent(BROADCAST_TIMER_STARTED);
                startIntent.putExtra(EXTRA_TASK_ID, mTaskId);
                LocalBroadcastManager.getInstance(this).sendBroadcast(startIntent);
                break;
            case ACTION_STOP_TIMER:
                if(mState != STATE_DEAD && mState != STATE_P_BREAK){
                    manageTimerStop();
                }
                stopSelf();
                break;
            case ACTION_START_BREAK:
                mCachedTaskId = mTaskId;
                manageTimerStop();
                mStartTime = System.currentTimeMillis();
                mState = STATE_P_BREAK;
                break;
        }
        return START_STICKY;
    }

    private void manageTimerStop() {
        //Record to save is:
        DatabaseEditor.getInstance(this).addNewRecord(
                mTaskId,
                (int) (System.currentTimeMillis() - mStartTime) / 1000,
                "Timed with LinCal timer",
                new Date(mStartTime),
                new Date(System.currentTimeMillis())
        );

        Intent startIntent = new Intent(BROADCAST_TIMER_STARTED);
        startIntent.putExtra(EXTRA_TASK_ID, mTaskId);
        startIntent.putExtra(EXTRA_ADDED_SECONDS, (int) (System.currentTimeMillis() - mStartTime) / 1000);
        mTaskId = -1;
        LocalBroadcastManager.getInstance(this).sendBroadcast(startIntent);
    }

    private void timerUpdate(){
        long now = System.currentTimeMillis();
        Pair<Integer, Integer> calc = calcMinsAndSecs(0);
        if(mState == STATE_P_BREAK && (mStartTime + BREAK_DURATION >= now)) calc = calcMinsAndSecs(BREAK_DURATION);
        String msg = calc.first + ":" + calc.second;
        if(calc.second < 10) msg = calc.first + ":0" + calc.second;

        switch(mState){
            case STATE_DEAD:
                return; //Weird.
            case STATE_P_BREAK:
                if(mStartTime + BREAK_DURATION == now){ //Exactly at end of break. Show "Break" notification
                    //Done with break! Annoy them once
                    NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle("Break's over")
                            .setContentText("Let's get back to working on " + mTask.getName())
                            .setPriority(Notification.PRIORITY_MAX)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Let's get back to working on " + mTask.getName()))
                            .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                            .setOngoing(true)
                            .setContentIntent(PendingIntent.getActivity(this, SHOW_ACTIVITY_PI_ID, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
                    startForeground(ONGOING_NID, nBuilder.build());
                }

                if (mStartTime + BREAK_DURATION > now){ //On break. Show "x:xx break time remaining"
                    updateNotification(mTaskDrawableId, "On break from '" + mTask.getName() + "'", calc.first + ":" + calc.second + " break time remaining");
                } else { //Overdue. Show "Overtime on break by x seconds!"
                    updateNotification(mTaskDrawableId, "Overtime on break!", "Late by " + calc.first + ":" + calc.second + "!");
                }
                break;
            case STATE_POMODORO:
                String pTitle = "Pomodoro timing '" + mTask.getName() + "'";
                if (mStartTime + POMODORO_DURATION > now){
                    updateNotification(mTaskDrawableId, pTitle, msg);
                } else { //Should break
                    updateNotification(mTaskDrawableId, pTitle, "Time to break! (" + msg + ")");
                }
                break;
            case STATE_RUNNING: //Show 'Timing xxx'
                updateNotification(mTaskDrawableId, "Timing '" + mTask.getName() + "'", msg);
                break;
        }
    }

    private Pair<Integer, Integer> calcMinsAndSecs(long furtherAdjust){
        long now = System.currentTimeMillis();
        long diff = now - mStartTime - furtherAdjust;

        long seconds = (int) (diff / 1000);
        int minutes = (int) Math.floor(seconds / 60);
        seconds = (int) (seconds - (minutes * 60));

        return new Pair<>(minutes, (int) seconds);
    }

    private void updateNotification(int drawable, String title, String message){
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(drawable)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, SHOW_ACTIVITY_PI_ID, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        Intent stopI = new Intent(this, TimerService.class);
        stopI.setAction(ACTION_STOP_TIMER);

        Intent breakI = new Intent(this, TimerService.class);
        breakI.setAction(ACTION_START_BREAK);

        Intent resumeI = new Intent(this, TimerService.class);
        resumeI.putExtra(EXTRA_TASK_ID, mCachedTaskId);
        resumeI.setAction(ACTION_START_POMODORO);

        PendingIntent stopIntent = PendingIntent.getService(this, STOP_TIMER_PI_ID, stopI, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent breakIntent = PendingIntent.getService(this, BREAK_TIMER_PI_ID, breakI, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent resumeIntent = PendingIntent.getService(this, RESUME_TIMER_PI_ID, resumeI, PendingIntent.FLAG_UPDATE_CURRENT);
        if(mState == STATE_RUNNING || (mState == STATE_POMODORO && (mStartTime + POMODORO_DURATION > System.currentTimeMillis())) || mState == STATE_P_BREAK){
            nBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_stop_black_24dp, "Stop", stopIntent));
        }
        if(mState == STATE_POMODORO && mStartTime + POMODORO_DURATION < System.currentTimeMillis()){
            nBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_mood_black_24dp, "Break", breakIntent));
        }
        if(mState == STATE_P_BREAK){
            nBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_play_circle_filled_black_24dp, "Resume", resumeIntent));
        }

        startForeground(ONGOING_NID, nBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
