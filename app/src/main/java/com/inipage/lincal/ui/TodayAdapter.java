package com.inipage.lincal.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inipage.lincal.R;
import com.inipage.lincal.Utilities;
import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.TaskToday;
import com.inipage.lincal.model.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class TodayAdapter extends TimerAwareAdapter<TodayAdapter.ItemVH> {
    public class ItemVH extends RecyclerView.ViewHolder {
        CardView cardView;
        View mainBackground;
        View sideBorder;
        View bottomBorder;
        View contentBorder;
        ImageView taskIcon;
        ImageView typeIcon;
        TextView headerText;
        ProgressBar progressBar;
        TextView contentText;
        View timerStart;
        View timerStop;
        View pomodoro;
        View checkbox;
        ImageView checkboxImage;

        public ItemVH(View itemView) {
            super(itemView);
            mainBackground = itemView.findViewById(R.id.main_content_view);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            sideBorder = itemView.findViewById(R.id.header_divider);
            bottomBorder = itemView.findViewById(R.id.header_bottom_divider);
            contentBorder = itemView.findViewById(R.id.main_content_bottom_divider);
            taskIcon = (ImageView) itemView.findViewById(R.id.today_icon);
            typeIcon = (ImageView) itemView.findViewById(R.id.today_type_icon);
            headerText = (TextView) itemView.findViewById(R.id.header_text);
            progressBar = (ProgressBar) itemView.findViewById(R.id.main_content_progress_bar);
            contentText = (TextView) itemView.findViewById(R.id.main_content_text_view);
            timerStart = itemView.findViewById(R.id.today_timer);
            timerStop = itemView.findViewById(R.id.today_stop);
            pomodoro = itemView.findViewById(R.id.today_pomodoro);
            checkbox = itemView.findViewById(R.id.today_checkbox);
            checkboxImage = (ImageView) itemView.findViewById(R.id.today_checkbox_iv);
        }
    }

    public static class TodayAdapterItem {
        private static final int TYPE_TODO = 1;
        private static final int TYPE_REMINDER = 2;

        private Object data;
        private int type;

        public TodayAdapterItem(Object data){
            if(data instanceof Todo){
                type = TYPE_TODO;
                this.data = data;
            } else if (data instanceof Task){
                type = TYPE_REMINDER;
                this.data = data;
            } else {
                throw new RuntimeException("Invalid entry!");
            }
        }

        public boolean isTodo(){
            return type == TYPE_TODO;
        }

        public boolean isReminder(){
            return type == TYPE_REMINDER;
        }

        public Todo getTodo(){
            return (Todo) data;
        }

        public TaskToday getTask(){
            return (TaskToday) data;
        }

        public static List<TodayAdapterItem> getStandardItems(Context ctx) {
            DatabaseEditor editor = DatabaseEditor.getInstance(ctx);

            //TODO: Speed up with SQL selections, limits
            List<TaskToday> tasks = editor.getTasksWithRemindersAndTimeSpentToday(false);
            List<Todo> todos = editor.getAllTodosSorted(null, true);

            List<TodayAdapterItem> items = new ArrayList<>();

            //Add the five soonest todos
            Calendar tmp = new GregorianCalendar();
            tmp.set(Calendar.HOUR_OF_DAY, 23);
            tmp.set(Calendar.MINUTE, 59);
            tmp.set(Calendar.SECOND, 59);
            tmp.roll(Calendar.DAY_OF_YEAR, 3);
            Date threeDaysFromNow = tmp.getTime();
            for (Todo t : todos) {
                if (!t.getDueDateAsDate().before(threeDaysFromNow)) continue;
                items.add(new TodayAdapterItem(t));
                if (items.size() > 4) break;
            }

            //Add all reminders that occur on today
            int today = new GregorianCalendar().get(Calendar.DAY_OF_WEEK); //0-6 moved to 1-7 to match Calendar format
            for (TaskToday t : tasks) {
                for (int dow : t.getReminderDowAsCalDow()) {
                    if (dow == today) {
                        items.add(new TodayAdapterItem(t));
                        break;
                    }
                }
            }

            //Sort sensibly
            //i.e. any todos for today (including complete), incomplete reminders for today, other todos, complete reminders
            Collections.sort(items, new Comparator<TodayAdapterItem>() {
                @Override
                public int compare(TodayAdapterItem lhs, TodayAdapterItem rhs) {
                    //Where we want to show left higher on the list = -1
                    //Where we want to show right higher on the list = -1
                    //Where we don't care = 0

                    //Check equality
                    if (lhs.isTodo() && rhs.isTodo() && lhs.getTodo().getDueDateAsDate().getDate()
                            == rhs.getTodo().getDueDateAsDate().getDate())
                        return 0;
                    if (lhs.isReminder() && rhs.isReminder() &&
                            ((!lhs.getTask().hasCompletedRequiredTime() && !rhs.getTask().hasCompletedRequiredTime()) ||
                                    (rhs.getTask().hasCompletedRequiredTime() && lhs.getTask().hasCompletedRequiredTime())))
                        return 0;

                    //compare todos
                    if (lhs.isTodo() && rhs.isTodo()) { //todos list doesn't contain complete ones; we need to compare today vs other days
                        //Equal case has been checked so one is newer
                        if (lhs.getTodo().getDueDateAsDate().before(rhs.getTodo().getDueDateAsDate())) //lhs before rhs; more important
                            return -1;
                        return 1;
                    } else if (rhs.isReminder() && lhs.isReminder()) { //tasks go: incomplete vs. complete
                        if (!lhs.getTask().hasCompletedRequiredTime() && rhs.getTask().hasCompletedRequiredTime())
                            return -1;
                        return 1;
                    } else { //one is todo, one is task -- tricky
                        int todayDate = new Date().getDate();
                        if (lhs.isTodo() && rhs.isReminder()) {
                            if (lhs.getTodo().getDueDateAsDate().getDate() == todayDate) { //Always ahead of reminder
                                return -1;
                            } else if (!rhs.getTask().hasCompletedRequiredTime()) {
                                return 1;
                            } else {
                                return -1; //other todos before complete stuff
                            }
                        }

                        if(lhs.isReminder() && rhs.isTodo()){ //Not necessary, but may as well
                            if(rhs.getTodo().getDueDateAsDate().getDate() == todayDate){
                                return 1;
                            } else if (!lhs.getTask().hasCompletedRequiredTime()) {
                                return -1;
                            } else {
                                return 1; //other todos before copmlete stuff
                            }
                        }
                    }

                    return 0;
                }
            });

            return items;
        }
    }

    List<TodayAdapterItem> mItems;
    Map<Long, Task> mTaskMap;
    Context mContext;

    public TodayAdapter(List<TodayAdapterItem> tasks, Context ctx){
        mItems = tasks;
        mContext = ctx;
        mTaskMap = DatabaseEditor.getInstance(ctx).getTaskMap();
    }

    @Override
    public ItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_today, parent, false));
    }

    private final SimpleDateFormat SHORT_DATE = new SimpleDateFormat("M/d", Locale.US);

    @Override
    public void onBindViewHolder(ItemVH holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(final ItemVH holder, int position) {
        TodayAdapterItem item = mItems.get(position);
        Task rootTask = item.isReminder() ? item.getTask() : mTaskMap.get(item.getTodo().getTaskId());

        //(1) Setup borders based on task color
        int color = rootTask.getColor();
        holder.bottomBorder.setBackgroundColor(color);
        holder.sideBorder.setBackgroundColor(color);
        holder.contentBorder.setBackgroundColor(color);

        //(2) Set task icon
        holder.taskIcon.setImageResource(mContext.getResources().getIdentifier(rootTask.getIcon(), "drawable", mContext.getPackageName()));

        //(3) Set type icon
        holder.typeIcon.setImageResource(item.isReminder() ? R.drawable.ic_assignment_black_24dp : R.drawable.ic_list_black_24dp);

        //(4) Set title (either to-do name or task title)
        holder.headerText.setText(item.isReminder() ? rootTask.getName() : item.getTodo().getTitle());

        //(5) Set progress bar, colors, and text
        if(item.isTodo()){
            holder.progressBar.setVisibility(View.GONE);

            Calendar today = new GregorianCalendar();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            if(item.getTodo().getDueDateAsDate().before(today.getTime())){
                holder.mainBackground.setBackgroundColor(Color.parseColor("#ffebee"));
            } else {
                holder.mainBackground.setBackgroundColor(Color.parseColor("#E8F5E9"));
            }

            holder.contentText.setText(mContext.getString(R.string.due_when, SHORT_DATE.format(item.getTodo().getDueDateAsDate())));
        } else {
            holder.progressBar.setVisibility(View.VISIBLE);

            int secondsSoFar = item.getTask().getSecondsSoFar();
            if(getTimerManager().isTimerRunning(item.getTask(), null)){
                //Uh-oh -- better grab "seconds so far" from the TimerService
                secondsSoFar += getTimerManager().getTimerTime();
            }

            holder.progressBar.setProgress(secondsSoFar);
            holder.progressBar.setMax(item.getTask().getReminderThreshold() * 60);

            if(secondsSoFar > (item.getTask().getReminderThreshold() * 60)) {
                holder.mainBackground.setBackgroundColor(Color.parseColor("#E8F5E9"));
                holder.contentText.setText("Goal met! (" + Utilities.formatDuration(0, 0, secondsSoFar, 0) + ")");
            } else {
                holder.mainBackground.setBackgroundColor(Color.parseColor("#ffebee"));
                holder.contentText.setText(Utilities.formatDuration(0, 0, secondsSoFar, 0) + "/" +
                        Utilities.formatDuration(0, item.getTask().getReminderThreshold(), 0, 0));
            }
        }

        //(6) Show/hide timing buttons if needed
        //Set button visibility
        if(item.isTodo()){
            boolean runningTimer = getTimerManager().isTimerRunning(mTaskMap.get(item.getTodo().getTaskId()), item.getTodo());
            holder.timerStop.setVisibility(runningTimer ? View.VISIBLE : View.GONE);
            holder.timerStart.setVisibility(runningTimer ? View.GONE : View.VISIBLE);
            holder.pomodoro.setVisibility(runningTimer ? View.GONE : View.VISIBLE);
        } else {
            boolean runningTimer = getTimerManager().isTimerRunning(item.getTask(), null);
            holder.timerStop.setVisibility(runningTimer ? View.VISIBLE : View.GONE);
            holder.timerStart.setVisibility(runningTimer ? View.GONE : View.VISIBLE);
            holder.pomodoro.setVisibility(runningTimer ? View.GONE : View.VISIBLE);
        }

        holder.timerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TodayAdapterItem item = mItems.get(holder.getAdapterPosition());

                Task task = item.isReminder() ? item.getTask() : mTaskMap.get(item.getTodo().getTaskId());
                Todo todo = item.isReminder() ? null : item.getTodo();

                getTimerManager().startTimer(task, todo);
            }
        });
        holder.pomodoro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TodayAdapterItem item = mItems.get(holder.getAdapterPosition());

                Task task = item.isReminder() ? item.getTask() : mTaskMap.get(item.getTodo().getTaskId());
                Todo todo = item.isReminder() ? null : item.getTodo();

                getTimerManager().startPomodoro(task, todo);
            }
        });
        holder.timerStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimerManager.stopTimer();
            }
        });

        //(7) Enable checkbox functionality
        if(item.isTodo()){
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.checkboxImage.setImageResource(item.getTodo().isComplete() ? R.drawable.ic_check_box_black_48dp : R.drawable.ic_check_box_outline_blank_black_48dp);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Todo todo = mItems.get(holder.getAdapterPosition()).getTodo();
                    boolean newState = !todo.isComplete();
                    DatabaseEditor.getInstance(mContext).updateTodo(todo.getId(), null, -1, null, -1, newState ? 1 : 0);
                    todo.setCompleted(newState);
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        } else {
            holder.checkbox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getTimerPosition(long taskId, long todoId) {
        for(int i = 0; i < mItems.size(); i++){
            TodayAdapterItem t = mItems.get(i);
            if(taskId != -1 && todoId != -1){
                if(!t.isTodo()) continue;
                if(t.getTodo().getId() == todoId && t.getTodo().getTaskId() == taskId) return i;
            } else if (taskId != -1){
                if(!t.isReminder()) continue;
                if(t.getTask().getId() == taskId) return i;
            }
        }

        return -1;
    }

    @Override
    public void onTimerStopped(TaskToday task, Todo todo) {
        //Before we update the item, we update the relevant item with the seconds in task
        //Yes, it's a 2xlinear search
        if(todo == null){
            int taskPosition = getTimerPosition(task.getId(), -1);
            if(taskPosition != -1) mItems.get(taskPosition).getTask().setSecondsSoFar(task.getSecondsSoFar());
        }
        super.onTimerStopped(task, todo);
    }
}
