package com.inipage.lincal;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Maclyn on 2/4/2017.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {
    public class TaskVH extends RecyclerView.ViewHolder {
        FrameLayout tabBackground;
        ImageView icon;
        TextView title;
        TextView subtitle;

        public TaskVH(View itemView) {
            super(itemView);
            tabBackground = (FrameLayout) itemView.findViewById(R.id.task_card_background);
            icon = (ImageView) itemView.findViewById(R.id.task_card_icon);
            title = (TextView) itemView.findViewById(R.id.task_name);
            subtitle = (TextView) itemView.findViewById(R.id.task_details);
        }
    }

    List<Task> mTasks;

    public TaskAdapter(List<Task> tasks){
        mTasks = tasks;
    }

    @Override
    public TaskVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(TaskVH holder, int position) {
        holder.tabBackground.setBackgroundColor(mTasks.get(position).getColor());
        Context ctx = holder.itemView.getContext();
        holder.icon.setImageResource(ctx.getResources().getIdentifier(mTasks.get(position).getIcon(), "drawable", ctx.getPackageName()));
        holder.title.setText(mTasks.get(position).getName());

        String dowStr = mTasks.get(position).getReminderDow();
        int dayCount = (dowStr == null || dowStr.isEmpty() ? 0 : dowStr.trim().split(" ").length);

        if(dayCount == 0 || mTasks.get(position).getReminderThreshold() == 0){
            holder.subtitle.setText("No reminder set");
        } else {
            holder.subtitle.setText(mTasks.get(position).getReminderThreshold() + " minutes, " + dayCount + "x/week");
        }
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }
}
