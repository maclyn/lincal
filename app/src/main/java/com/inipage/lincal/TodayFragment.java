package com.inipage.lincal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TodayFragment extends Fragment {
    RecyclerView taskView;
    TodayAdapter adapter;
    BroadcastReceiver receiver;

    public TodayFragment(){
    }

    public static TodayFragment newInstance(){
        return new TodayFragment();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_today, container, false);
        taskView = (RecyclerView) v.findViewById(R.id.today_view);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        taskView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final List<TaskToday> tasks = DatabaseEditor.getInstance(getActivity()).getTasksWithTimeSpentToday();
        Collections.sort(tasks, new Comparator<TaskToday>() {
            @Override
            public int compare(TaskToday taskToday, TaskToday t1) {
                return taskToday.getSecondsSoFar() - t1.getSecondsSoFar();
            }
        });
        adapter = new TodayAdapter(tasks);
        taskView.setAdapter(adapter);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int taskLocation = 0;
                for(TaskToday t : tasks){
                    if(t.getId() == intent.getLongExtra(TimerService.EXTRA_TASK_ID, -1))
                        break;
                    taskLocation++;
                }
                tasks.get(taskLocation).setSecondsSoFar(tasks.get(taskLocation).getSecondsSoFar() + intent.getIntExtra(TimerService.EXTRA_ADDED_SECONDS, 0));
                adapter.notifyItemChanged(taskLocation);
            }
        };
        IntentFilter filter = new IntentFilter(TimerService.BROADCAST_TIMER_STARTED);
        filter.addAction(TimerService.BROADCAST_TIMER_STOPPED);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }
}
