package com.inipage.lincal.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inipage.lincal.R;
import com.inipage.lincal.model.Task;

import java.util.List;

/**
 * Created by Maclyn on 2/9/2017.
 * TODO: Add a JavaDoc for this class.
 */

public class TaskNameAdapter extends ArrayAdapter<Task> {
    public TaskNameAdapter(Context context, int resource, List<Task> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getViewImpl(position, convertView, parent, true);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewImpl(position, convertView, parent, false);
    }

    private View getViewImpl(int position, View convertView, ViewGroup parent, boolean isDropDown){
        Task t = getItem(position);
        View itemView = convertView; //TODO: Eventually use old-school ViewHolder pattern here
        if(itemView == null)
            itemView = LayoutInflater.from(getContext()).inflate(
                    isDropDown ? R.layout.item_task_spinner : R.layout.item_task_spinner_fixed, parent, false);

        ((ImageView) itemView.findViewById(R.id.task_spinner_icon)).setImageResource(
                getContext().getResources().getIdentifier(t.getIcon(), "drawable", getContext().getPackageName()));
        ((TextView) itemView.findViewById(R.id.task_spinner_text)).setText(t.getName());

        return itemView;
    }
}
