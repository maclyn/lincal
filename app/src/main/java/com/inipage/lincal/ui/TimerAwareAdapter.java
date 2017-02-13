package com.inipage.lincal.ui;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.inipage.lincal.background.TimerStateManager;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;
import com.inipage.lincal.model.Todo;

/**
 * An adapter which cares about timers in our application. Helps us take care of some messy state stuff.
 * @param <ViewHolderType> A RecyclerView ViewHolder.
 */
public abstract class TimerAwareAdapter<ViewHolderType extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ViewHolderType>
        implements TimerStateManager.TimerStateListener{
    protected TimerStateManager mTimerManager;
    private Handler mHandler;

    @Override
    public abstract ViewHolderType onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(ViewHolderType holder, int position);

    @Override
    public abstract int getItemCount();

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mTimerManager = TimerStateManager.getInstance(recyclerView.getContext());
        mTimerManager.registerTimerListener(this);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mTimerManager.unregisterTimerListener(this);
        mTimerManager = null;
        mHandler = null;
    }

    public abstract int getTimerPosition(long taskId, long todoId);

    @Override
    public void onTimerStarted(TaskToday task, Todo todo) {
        final int timerPos = getTimerPosition(task != null ? task.getId() : -1, todo != null ? todo.getId() : -1);
        if(timerPos != -1){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(timerPos);
                }
            });
        }
    }

    @Override
    public void onTimerStopped(TaskToday task, Todo todo) {
        final int timerPos = getTimerPosition(task != null ? task.getId() : -1, todo != null ? todo.getId() : -1);
        if(timerPos != -1){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(timerPos);
                }
            });
        }
    }

    @Override
    public void onTimerTicked(TaskToday task, Todo todo) {
        final int timerPos = getTimerPosition(task != null ? task.getId() : -1, todo != null ? todo.getId() : -1);
        if(timerPos != -1){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(timerPos);
                }
            });
        }
    }

    protected TimerStateManager getTimerManager(){
        return mTimerManager;
    }
}
