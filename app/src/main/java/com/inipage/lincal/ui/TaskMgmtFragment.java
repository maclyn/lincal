package com.inipage.lincal.ui;

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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inipage.lincal.AddTaskActivity;
import com.inipage.lincal.R;
import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;

import java.util.List;

public class TaskMgmtFragment extends TimerAwareFragment {
    FloatingActionButton addTask;
    RecyclerView taskView;
    Toolbar toolbar;
    TaskAdapter adapter;

    List<Task> tasks;
    boolean includeArchived = false;

    BroadcastReceiver timerReceiver;

    public TaskMgmtFragment(){
    }

    public static TaskMgmtFragment newInstance(){
        return new TaskMgmtFragment();
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
        View v = inflater.inflate(R.layout.fragment_task_mgmt, container, false);

        taskView = (RecyclerView) v.findViewById(R.id.task_view);
        addTask = (FloatingActionButton) v.findViewById(R.id.add_task);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddTaskActivity.class));
            }
        });
        taskView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayout.VERTICAL));
        toolbar = (Toolbar) v.findViewById(R.id.task_toolbar);
        toolbar.inflateMenu(R.menu.task_menu);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        setAdapter();
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.show_archived:
                        includeArchived = !item.isChecked();
                        item.setChecked(includeArchived);
                        setAdapter();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    void onTimerStarted(long taskId, long todoId) {
        int taskLocation = 0;
        for(Task t : tasks){
            if(t.getId() == taskId)
                break;
            taskLocation++;
        }
        adapter.notifyItemChanged(taskLocation);
    }

    @Override
    void onTimerStopped(long taskId, long todoId) {
        onTimerStarted(taskId, todoId); //Same implementation, actually
    }

    private void setAdapter(){
        taskView.setLayoutManager(new LinearLayoutManager(getActivity()));
        tasks = DatabaseEditor.getInstance(getActivity()).getTasks(includeArchived);
        adapter = new TaskAdapter(tasks,
            new TaskAdapter.ReloadListener() {
                @Override
                public void onReloadNeeded() {
                    setAdapter();
                }
            });
        taskView.setAdapter(adapter);
    }
}
