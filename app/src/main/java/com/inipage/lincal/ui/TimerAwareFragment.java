package com.inipage.lincal.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.model.Task;

/**
 * A fragment with built in support for being updated when the timer does stuff.
 */
public abstract class TimerAwareFragment extends Fragment {
    private BroadcastReceiver mTimerReceiver;

    @Override
    public void onResume() {
        super.onResume();

        mTimerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long taskId = intent.getLongExtra(TimerService.EXTRA_TASK_ID, -1);
                long todoId = intent.getLongExtra(TimerService.EXTRA_TODO_ID, -1);

                if(intent.getAction().equals(TimerService.BROADCAST_TIMER_STARTED)){
                    onTimerStarted(taskId, todoId);
                } else if (intent.getAction().equals(TimerService.BROADCAST_TIMER_STOPPED)){
                    onTimerStopped(taskId, todoId);
                }
            }
        };
        IntentFilter filter = new IntentFilter(TimerService.BROADCAST_TIMER_STARTED);
        filter.addAction(TimerService.BROADCAST_TIMER_STOPPED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mTimerReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mTimerReceiver);
    }

    abstract void onTimerStarted(long taskId, long todoId);

    abstract void onTimerStopped(long taskId, long todoId);
}
