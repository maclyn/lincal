package com.inipage.lincal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TaskMgmtFragment extends Fragment {
    FloatingActionButton addTask;
    RecyclerView taskView;

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

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        taskView.setLayoutManager(new LinearLayoutManager(getActivity()));
        taskView.setAdapter(new TaskAdapter(DatabaseEditor.getInstance(getActivity()).getTasks()));
    }
}
