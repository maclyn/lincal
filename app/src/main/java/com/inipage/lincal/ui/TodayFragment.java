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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inipage.lincal.R;
import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TodayFragment extends TimerAwareFragment {
    RecyclerView todayView;
    TodayAdapter adapter;
    List<TodayAdapter.TodayAdapterItem> items;

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
        todayView = (RecyclerView) v.findViewById(R.id.today_view);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        items = TodayAdapter.TodayAdapterItem.getStandardItems(getContext());
        todayView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new TodayAdapter(items, getContext());
        todayView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    void onTimerStarted(long taskId, long todoId) {
        int taskLocation = 0;
        for(TodayAdapter.TodayAdapterItem t : items){
            if(t.isReminder() && todoId == -1 && t.getTask().getId() == taskId)
                break;
            else if(t.isTodo() && todoId != -1 && t.getTodo().getId() == todoId && t.getTodo().getTaskId() == taskId)
                break;
            taskLocation++;
        }
        adapter.notifyItemChanged(taskLocation);
    }

    @Override
    void onTimerStopped(long taskId, long todoId) {
        onTimerStarted(taskId, todoId); //Same implementation, actually
    }
}
