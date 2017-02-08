package com.inipage.lincal;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {
    private static final String TAG = "TaskAdapter";

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
    public void onBindViewHolder(final TaskVH holder, final int position) {
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

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //TODO: Handle stop of timer service iff we're deleting the task running on timer

                int position = holder.getAdapterPosition();
                //Delete records & task
                DatabaseEditor.getInstance(view.getContext()).deleteTask(mTasks.get(position).getId());
                mTasks.remove(position);
                notifyItemRemoved(position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }
}
