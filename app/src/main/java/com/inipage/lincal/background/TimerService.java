package com.inipage.lincal.background;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;

import com.inipage.lincal.MainActivity;
import com.inipage.lincal.R;
import com.inipage.lincal.Utilities;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;
import com.inipage.lincal.model.Todo;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO: This class *desperately* needs some synchronization primitives.
 * Looks like stuff is getting run on multiple threads for some reason.
 */
public class TimerService extends Service implements TimerStateManager.TimerStateController {
    /** ACTIONs and EXTRAs to instrument this service. Shouldn't really be used; prefer {@linkplain TimerStateManager}.**/

    static final String ACTION_START_TIMER = "start_timer";
    static final String ACTION_STOP_TIMER = "stop_timer";
    static final String ACTION_START_POMODORO = "start_pomdoro";
    static final String ACTION_START_BREAK = "start_break";

    static final String EXTRA_TASK_ID = "task_id";
    static final String EXTRA_TODO_ID = "todo_id";

    private static final int ONGOING_NID = 500;

    private static final int SHOW_ACTIVITY_PI_ID = 1000;
    private static final int STOP_TIMER_PI_ID = 1001;
    private static final int RESUME_TIMER_PI_ID = 1002;
    private static final int BREAK_TIMER_PI_ID = 1003;

    /** Internal service semantics. **/
    private static final long POMODORO_DURATION = 25 * 60 * 1000; //25 minutes
    private static final long BREAK_DURATION = 5 * 60 * 1000; //5 minutes

    private static final int STATE_DEAD = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_POMODORO = 2;
    private static final int STATE_P_BREAK = 3;

    //Gross runtime state stuff.
    private final Object stateLock = new Object();
    private int mState = STATE_DEAD;
    private long mStartTime = -1L;
    private TaskToday mTask = null;
    private Todo mTodo = null;
    private int mTaskDrawableId = -1;
    private Timer mTimer = new Timer();
    private TimerStateManager mTimerStateManager;

    public TimerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimerStateManager = TimerStateManager.getInstance(this);
        mTimerStateManager.registerTimerController(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimerStateManager.unregisterTimerController();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) return START_NOT_STICKY;
        String action = intent.getAction();

        switch(action){
            case ACTION_START_TIMER:
            case ACTION_START_POMODORO:
                //Start a timer for a given taskId
                long mTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
                long mTodoId = intent.getLongExtra(EXTRA_TODO_ID, -1);

                startTimer(DatabaseEditor.getInstance(this).getTask(mTaskId),
                        mTodoId != -1 ? DatabaseEditor.getInstance(this).getTodo(mTodoId) : null,
                        !action.equals(ACTION_START_TIMER));
                break;
            case ACTION_STOP_TIMER:
                Utilities.debugNotification("Got stop timer", "!", this);

                stopTimer();
                stopSelf();
                break;
            case ACTION_START_BREAK:
                TaskToday mCachedTask = mTask;
                Todo mCachedTodo = mTodo;

                //TODO: I need a lock somewhere here, don't I? The timer seems precariously subject to race conditions.
                stopTimer();

                mStartTime = System.currentTimeMillis();
                mState = STATE_P_BREAK;
                mTask = mCachedTask;
                mTodo = mCachedTodo;
                break;
        }
        return START_STICKY;
    }

    private void timerUpdate(){
        if(mTask == null){
            stopSelf();
            return;
        }

        long now = System.currentTimeMillis();
        Pair<Integer, Integer> calc = calcMinsAndSecs(0);
        if(mState == STATE_P_BREAK && (mStartTime + BREAK_DURATION >= now)) calc = calcMinsAndSecs(BREAK_DURATION);
        String msg = calc.first + ":" + calc.second;
        String timingName = (mTodo == null ? mTask.getName() : mTodo.getTitle());
        if(calc.second < 10) msg = calc.first + ":0" + calc.second;

        switch(mState){
            case STATE_DEAD:
                return; //Weird.
            case STATE_P_BREAK:
                if(mStartTime + BREAK_DURATION == now){ //Exactly at end of break. Show "Break" notification
                    //Done with break! Annoy them once
                    NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                            .setContentTitle("Break's over")
                            .setContentText("Let's get back to working on " + timingName)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Let's get back to working on " + timingName))
                            .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                            .setOngoing(true)
                            .setContentIntent(PendingIntent.getActivity(this, SHOW_ACTIVITY_PI_ID, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
                    startForeground(ONGOING_NID, nBuilder.build());
                }

                if (mStartTime + BREAK_DURATION > now){ //On break. Show "x:xx break time remaining"
                    updateNotification(mTaskDrawableId, "On break from '" + timingName + "'", calc.first + ":" + calc.second + " break time remaining");
                } else { //Overdue. Show "Overtime on break by x seconds!"
                    updateNotification(mTaskDrawableId, "Overtime on break!", "Late by " + calc.first + ":" + calc.second + "!");
                }
                break;
            case STATE_POMODORO:
                String pTitle = "Pomodoro timing '" + timingName + "'";
                if (mStartTime + POMODORO_DURATION > now){
                    updateNotification(mTaskDrawableId, pTitle, msg);
                } else { //Should break
                    updateNotification(mTaskDrawableId, pTitle, "Time to break! (" + msg + ")");
                }
                break;
            case STATE_RUNNING: //Show 'Timing xxx'
                updateNotification(mTaskDrawableId, "Timing '" + timingName + "'", msg);
                break;
        }

        mTask.setSecondsSoFar(mTask.getSecondsSoFar() + 1);
        mTimerStateManager.onTimerTicked(mTask, mTodo);
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
        resumeI.putExtra(EXTRA_TASK_ID, mTask.getId());
        if(mTodo != null)
            resumeI.putExtra(EXTRA_TODO_ID, mTodo.getId());
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

    @Override
    public void startTimer(Task task, Todo todo, boolean isPomodoro) {
        synchronized (stateLock) {
            if (mState != STATE_DEAD && mState != STATE_P_BREAK) {
                stopTimer();
            }

            mTask = DatabaseEditor.getInstance(this).getTaskWithTimes(task.getId());
            mTodo = todo;
            mTaskDrawableId = getResources().getIdentifier(mTask.getIcon(), "drawable", getPackageName());
            mState = !isPomodoro ? STATE_RUNNING : STATE_POMODORO;
            mTimerStateManager.onTimerStarted(mTask, todo);
            TimerTask updateTimer = new TimerTask() {
                @Override
                public void run() {
                    timerUpdate();
                }
            };
            mStartTime = System.currentTimeMillis();
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(updateTimer, new Date(), 1000L);
        }
    }

    @Override
    public void stopTimer() {
        TaskToday cachedTask = null;
        Todo cachedTodo = null;

        synchronized (stateLock) {
            if (mState == STATE_DEAD || mState == STATE_P_BREAK) {
                return;
            }

            int secondsRun = (int) (System.currentTimeMillis() - mStartTime) / 1000;
            //We do this to reconcile *rough* timing (+1 in ticks) with actually timing
            mTask.setSecondsSoFar(DatabaseEditor.getInstance(this).getTaskWithTimes(mTask.getId()).getSecondsSoFar() + secondsRun);

            DatabaseEditor.getInstance(this).addNewRecord(
                    mTask.getId(),
                    secondsRun,
                    mTodo != null ? getString(R.string.worked_on_todo, mTodo.getTitle()) : getString(R.string.timed_with_timer),
                    new Date(mStartTime),
                    new Date(System.currentTimeMillis()),
                    mTodo == null ? -1 : mTodo.getId()
            );

            mTimer.cancel();

            cachedTask = mTask;
            cachedTodo = mTodo;
            mTask = null;
            mTodo = null;
            mState = STATE_DEAD;

            stopForeground(true);
        }
        mTimerStateManager.onTimerStopped(cachedTask, cachedTodo);
    }

    @Override
    public boolean isTimerRunning(long taskId, long todoId) {
        synchronized (stateLock) {
            if (mState == STATE_DEAD || mState == STATE_P_BREAK) return false;
            if (mTask == null) return false;

            if(todoId == -1){ //Only check task
                return mTask.getId() == taskId;
            } else {
                return mTodo != null && mTodo.getId() == todoId;
            }
        }
    }

    @Override
    public int getTimerTime() {
        synchronized (stateLock) {
            if (mState == STATE_RUNNING || mState == STATE_POMODORO) {
                long now = System.currentTimeMillis();
                long diff = now - mStartTime;
                return (int) (diff / 1000);
            }
            return 0;
        }
    }
}
