package com.inipage.lincal.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inipage.lincal.R;
import com.inipage.lincal.db.DatabaseEditor;

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
        historyView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
