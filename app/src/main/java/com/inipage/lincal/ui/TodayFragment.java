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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inipage.lincal.R;
import com.inipage.lincal.Utilities;
import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TodayFragment extends Fragment {
    RecyclerView todayView;
    TodayAdapter adapter;
    List<TodayAdapter.TodayAdapterItem> items;

    View progressDivider;
    ProgressBar todoProgress;
    ProgressBar reminderProgress;
    TextView todoText;
    TextView reminderText;

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
        todayView.addItemDecoration(new GridSpacingItemDecoration(2, (int) getContext().getResources().getDimension(R.dimen.card_margin)));
        todayView.getItemAnimator().setChangeDuration(0);
        progressDivider = v.findViewById(R.id.progress_divider);
        todoProgress = (ProgressBar) v.findViewById(R.id.today_todo_progress);
        reminderProgress = (ProgressBar) v.findViewById(R.id.today_reminder_progress);
        todoText = (TextView) v.findViewById(R.id.today_todo_text);
        reminderText = (TextView) v.findViewById(R.id.today_reminder_text);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        items = TodayAdapter.TodayAdapterItem.getStandardItems(getContext());
        todayView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new TodayAdapter(items, getContext());
        todayView.setAdapter(adapter);

        updateProgressBars();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateProgressBars() {
        int todayTodoCount = 0;
        int todayCompleteTodoCount = 0;
        int todayReminderCount = 0;
        int todayCompleteReminderCount = 0;

        Date todayAtMidnight = Utilities.getTodayAtMidnight();
        for(TodayAdapter.TodayAdapterItem item : items){
            if(item.isReminder()){
                todayReminderCount++;
                if(item.getTask().hasCompletedRequiredTime()) todayCompleteReminderCount++;
            } else {
                if(item.getTodo().getDueDateAsDate().before(todayAtMidnight)){
                    todayTodoCount++;
                    if(item.getTodo().isComplete()) todayCompleteTodoCount++;
                }
            }
        }

        //Hide/show stuff as needed
        progressDivider.setVisibility(View.VISIBLE);
        reminderProgress.setVisibility(View.VISIBLE);
        todoProgress.setVisibility(View.VISIBLE);
        todoText.setVisibility(View.VISIBLE);
        reminderText.setVisibility(View.VISIBLE);
        if(todayReminderCount == 0 && todayTodoCount == 0){
            progressDivider.setVisibility(View.GONE);
            reminderProgress.setVisibility(View.GONE);
            todoProgress.setVisibility(View.GONE);
            todoText.setVisibility(View.GONE);

            reminderText.setText(R.string.nothing_todo_today);
            return;
        } else if (todayReminderCount == 0){
            progressDivider.setVisibility(View.GONE);
            reminderProgress.setVisibility(View.GONE);
            reminderText.setVisibility(View.GONE);
        } else if (todayTodoCount == 0){
            progressDivider.setVisibility(View.GONE);
            todoProgress.setVisibility(View.GONE);
            todoText.setVisibility(View.GONE);
        }

        if(todayReminderCount != 0){
            reminderProgress.setMax(todayReminderCount);
            reminderProgress.setProgress(todayCompleteReminderCount);
            reminderText.setText(getString(R.string.reminder_count, todayCompleteReminderCount, todayReminderCount));
        }

        if(todayTodoCount != 0){
            todoProgress.setMax(todayTodoCount);
            todoProgress.setProgress(todayCompleteTodoCount);
            todoText.setText(getString(R.string.todo_count, todayCompleteTodoCount, todayTodoCount));
        }
    }
}
