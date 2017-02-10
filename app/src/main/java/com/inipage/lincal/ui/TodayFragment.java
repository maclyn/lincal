package com.inipage.lincal.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inipage.lincal.R;
import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.TaskToday;

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

        /*
        taskView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final List<TaskToday> tasks = DatabaseEditor.getInstance(getActivity()).getTasksWithRemindersAndTimeSpentToday(false);
        Collections.sort(tasks, new Comparator<TaskToday>() {
            @Override
            public int compare(TaskToday taskToday, TaskToday t1) {
                return taskToday.getSecondsSoFar() - t1.getSecondsSoFar();
            }
        });
        adapter = new TodayAdapter(tasks);
        taskView.setAdapter(adapter);
            */
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
