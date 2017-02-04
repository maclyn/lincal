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

public class HistoryFragment extends Fragment {
    RecyclerView historyView;
    HistoryAdapter adapter;

    public HistoryFragment(){
    }

    public static HistoryFragment newInstance(){
        return new HistoryFragment();
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
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        historyView = (RecyclerView) v.findViewById(R.id.history_view);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        historyView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new HistoryAdapter(DatabaseEditor.getInstance(getActivity()).getRecordsSortedByDay());
        historyView.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
