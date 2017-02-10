package com.inipage.lincal.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inipage.lincal.R;
import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.TaskVH> {
    public class TaskVH extends RecyclerView.ViewHolder {
        CardView cardView;
        FrameLayout tabBackground;
        ImageView icon;
        TextView title;
        ProgressBar amountComplete;
        Button start;
        Button pomodoro;
        Button stop;
        TextView timeToday;

        public TaskVH(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            timeToday = (TextView) itemView.findViewById(R.id.task_amount_today);
            tabBackground = (FrameLayout) itemView.findViewById(R.id.task_card_background);
            icon = (ImageView) itemView.findViewById(R.id.task_card_icon);
            title = (TextView) itemView.findViewById(R.id.task_name);
            amountComplete = (ProgressBar) itemView.findViewById(R.id.task_progress);
            start = (Button) itemView.findViewById(R.id.start_button);
            pomodoro = (Button) itemView.findViewById(R.id.pomodoro_button);
            stop = (Button) itemView.findViewById(R.id.stop_button);
        }
    }

    List<TaskToday> mTasks;

    public TodayAdapter(List<TaskToday> tasks){
        mTasks = tasks;
    }

    @Override
    public TaskVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_today, parent, false));
    }

    @Override
    public void onBindViewHolder(final TaskVH holder, int position) {
        holder.tabBackground.setBackgroundColor(mTasks.get(position).getColor());
        Context ctx = holder.itemView.getContext();
        holder.icon.setImageResource(ctx.getResources().getIdentifier(mTasks.get(position).getIcon(), "drawable", ctx.getPackageName()));
        holder.title.setText(mTasks.get(position).getName());
        int secondsSoFar = mTasks.get(position).getSecondsSoFar();
        holder.cardView.setCardBackgroundColor(ctx.getResources().getColor(R.color.white));

        Task task = mTasks.get(position);
        holder.amountComplete.setVisibility(View.GONE);

        reminderBarSetter: {
            if (task.getReminderThreshold() != 0) {
                //Not _necessarily_ important this day of week
                int todayDow = new GregorianCalendar().get(Calendar.DAY_OF_WEEK);
                boolean validDay = false;
                for (int dow : task.getReminderDowAsCalDow()) {
                    if (dow == todayDow) {
                        validDay = true;
                        break;
                    }
                }
                if(!validDay) break reminderBarSetter;

                holder.amountComplete.setVisibility(View.VISIBLE);
                holder.amountComplete.setMax(mTasks.get(position).getReminderThreshold() * 60);
                holder.amountComplete.setProgress(secondsSoFar);

                if (secondsSoFar >= mTasks.get(position).getReminderThreshold() * 60) {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
                } else {
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#ffcdd2"));
                }
            }
        }

        int minutes = (int) Math.floor(secondsSoFar / 60);
        int seconds = (secondsSoFar - (minutes * 60));
        holder.timeToday.setText(minutes + "m" + (seconds > 10 ? seconds : "0" + seconds) + "s today");

        boolean runningTimer = mTasks.get(position).getId() == TimerService.mTaskId;
        holder.stop.setVisibility(runningTimer ? View.VISIBLE : View.GONE);
        holder.start.setVisibility(runningTimer ? View.GONE : View.VISIBLE);
        holder.pomodoro.setVisibility(runningTimer ? View.GONE : View.VISIBLE);

        holder.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TaskToday task = mTasks.get(holder.getAdapterPosition());
                Context ctx = holder.itemView.getContext();

                Intent serviceIntent = new Intent(ctx, TimerService.class);
                serviceIntent.setAction(TimerService.ACTION_START_TIMER);
                serviceIntent.putExtra(TimerService.EXTRA_TASK_ID, task.getId());
                ctx.startService(serviceIntent);
            }
        });
        holder.pomodoro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TaskToday task = mTasks.get(holder.getAdapterPosition());
                Context ctx = holder.itemView.getContext();

                Intent serviceIntent = new Intent(ctx, TimerService.class);
                serviceIntent.setAction(TimerService.ACTION_START_POMODORO);
                serviceIntent.putExtra(TimerService.EXTRA_TASK_ID, task.getId());
                ctx.startService(serviceIntent);
            }
        });
        holder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TaskToday task = mTasks.get(holder.getAdapterPosition());
                Context ctx = holder.itemView.getContext();

                Intent serviceIntent = new Intent(ctx, TimerService.class);
                serviceIntent.setAction(TimerService.ACTION_STOP_TIMER);
                serviceIntent.putExtra(TimerService.EXTRA_TASK_ID, task.getId());
                ctx.startService(serviceIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }
}
