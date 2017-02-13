package com.inipage.lincal.background;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;
import com.inipage.lincal.model.Todo;

import java.util.ArrayList;
import java.util.List;

/**
 * A in-memory only object that allows various components to register to receive information about
 * timer state updates.
 */
public class TimerStateManager {
    public interface TimerStateListener {
        /**
         * Called when a timer is started.
         * @param task The task.
         * @param todo The todo. Possibly null.
         */
        void onTimerStarted(TaskToday task, Todo todo);

        void onTimerStopped(TaskToday task, Todo todo);

        /**
         * Called when a timer ticks.
         * @param task The task. Note the seconds data *will* be up to date.
         * @param todo The todo. Possibly null.
         */
        void onTimerTicked(TaskToday task, Todo todo);
    }

    public interface TimerStateController {
        void startTimer(Task task, @Nullable Todo todo, boolean isPomodoro);
        void stopTimer();
        boolean isTimerRunning(long taskId, long todoId);
        int getTimerTime();
    }

    private static TimerStateManager mInstance;
    private TimerStateController mTimerController;
    private List<TimerStateListener> mTimerListeners;
    private Context mContext;

    private TimerStateManager(Context ctx){
        mTimerListeners = new ArrayList<>();
        mTimerController = null;
        mContext = ctx;
    }

    public static TimerStateManager getInstance(Context ctx){
        return mInstance == null ? (mInstance = new TimerStateManager(ctx)) : mInstance;
    }

    public void registerTimerController(TimerStateController controller){
        this.mTimerController = controller;
    }

    public void unregisterTimerController(){
        this.mTimerController = null;
    }

    public void registerTimerListener(TimerStateListener listener){
        mTimerListeners.add(listener);
    }

    public void unregisterTimerListener(TimerStateListener listener){
        mTimerListeners.remove(listener);
    }

    public void startTimer(Task task, Todo todo){
        if(mTimerController != null){
            mTimerController.startTimer(task, todo, false);
        } else {
            Intent serviceIntent = new Intent(mContext, TimerService.class);
            serviceIntent.setAction(TimerService.ACTION_START_TIMER);
            serviceIntent.putExtra(TimerService.EXTRA_TASK_ID, task.getId());
            if(todo != null)
                serviceIntent.putExtra(TimerService.EXTRA_TODO_ID, todo.getId());
            mContext.startService(serviceIntent);
        }
    }

    public void startPomodoro(Task task, Todo todo){
        if(mTimerController != null){
            mTimerController.startTimer(task, todo, true);
        } else {
            Intent serviceIntent = new Intent(mContext, TimerService.class);
            serviceIntent.setAction(TimerService.ACTION_START_POMODORO);
            serviceIntent.putExtra(TimerService.EXTRA_TASK_ID, task.getId());
            if(todo != null)
                serviceIntent.putExtra(TimerService.EXTRA_TODO_ID, todo.getId());
            mContext.startService(serviceIntent);
        }
    }

    /**
     * Stop the currently running timer.
     */
    public void stopTimer(){
        if(mTimerController != null){
            mTimerController.stopTimer();
        } else {
            Intent serviceIntent = new Intent(mContext, TimerService.class);
            serviceIntent.setAction(TimerService.ACTION_STOP_TIMER);
            mContext.startService(serviceIntent);
        }
    }

    public void onTimerStarted(TaskToday task, Todo todo){
        for(TimerStateListener l : mTimerListeners){
            l.onTimerStarted(task, todo);
        }
    }

    public void onTimerStopped(TaskToday task, Todo todo) {
        for(TimerStateListener l : mTimerListeners){
            l.onTimerStopped(task, todo);
        }
    }

    public void onTimerTicked(TaskToday task, Todo todo){
        for(TimerStateListener l : mTimerListeners){
            l.onTimerTicked(task, todo);
        }
    }

    public boolean isTimerRunning(Task task, Todo todo){
        return mTimerController != null &&
                mTimerController.isTimerRunning(task != null ? task.getId() : -1,
                                                todo != null ? todo.getId() : -1);
    }

    public int getTimerTime() {
        return (mTimerController == null ? 0 : mTimerController.getTimerTime());
    }
}
