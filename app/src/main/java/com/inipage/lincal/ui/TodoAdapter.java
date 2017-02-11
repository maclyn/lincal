package com.inipage.lincal.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.inipage.lincal.R;
import com.inipage.lincal.Utilities;
import com.inipage.lincal.background.ReminderService;
import com.inipage.lincal.background.TimerService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.Todo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoVH> {
    class TodoVH extends RecyclerView.ViewHolder {
        TextView todoTitle;
        ImageView todoCheck;
        View todoTimerStart;
        View todoTimerStop;
        View todoDueDateButton;
        View todoImportanceButton;
        View todoTaskButton;
        TextView todoDudeDateText;
        TextView todoImportanceText;
        ImageView todoTaskIcon;

        public TodoVH(View itemView) {
            super(itemView);

            todoTitle = (TextView) itemView.findViewById(R.id.todo_title);
            todoCheck = (ImageView) itemView.findViewById(R.id.todo_checkbox);
            todoDueDateButton = itemView.findViewById(R.id.todo_date_button);
            todoDudeDateText = (TextView) itemView.findViewById(R.id.todo_date);
            todoImportanceButton = itemView.findViewById(R.id.todo_importance_button);
            todoImportanceText = (TextView) itemView.findViewById(R.id.todo_importance);
            todoTaskButton = itemView.findViewById(R.id.todo_task_button);
            todoTaskIcon = (ImageView) itemView.findViewById(R.id.todo_task);
            todoTimerStart = itemView.findViewById(R.id.todo_timer);
            todoTimerStop = itemView.findViewById(R.id.todo_stop);
        }
    }

    List<Todo> mTodos;
    List<Task> mCachedTasks;
    long mCachedTimerTodoId = -1;
    int mCachedTimerPosition = -1;

    public TodoAdapter(List<Todo> todos, List<Task> tasks){
        mTodos = todos;
        mCachedTasks = tasks;

        if(TimerService.mTodoId != -1){
            int search = searchForMatchingTimer(TimerService.mTodoId);
            if(search != -1){
                mCachedTimerTodoId = TimerService.mTodoId;
                mCachedTimerPosition = search;
            }
            searchForMatchingTimer(TimerService.mTodoId);
        }
    }

    @Override
    public TodoVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false));
    }

    private final SimpleDateFormat SHORT_DATE = new SimpleDateFormat("M/d", Locale.US);

    @Override
    public void onBindViewHolder(final TodoVH holder, int position) {
        Todo todo = mTodos.get(position);
        for(Task t : mCachedTasks){ //TODO: Map tasks -> icons > O(n) search?
            if(t.getId() == todo.getTaskId()){
                Context ctx = holder.itemView.getContext();
                holder.todoTaskIcon.setImageResource(ctx.getResources().getIdentifier(t.getIcon(),
                        "drawable", ctx.getPackageName()));
                break;
            }
        }
        holder.todoTitle.setText(todo.getTitle());
        holder.todoCheck.setImageResource(todo.isComplete() ? R.drawable.ic_check_box_black_48dp :
            R.drawable.ic_check_box_outline_blank_black_48dp);
        holder.todoDudeDateText.setText(SHORT_DATE.format(todo.getDueDateAsDate()));
        holder.todoImportanceText.setText(String.valueOf(todo.getImportance()));
        holder.todoImportanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImportance(v.getContext(), holder.getAdapterPosition());
            }
        });
        holder.todoDueDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateChanger(v.getContext(), holder.getAdapterPosition());
            }
        });
        holder.todoTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTaskChange(v.getContext(), holder.getAdapterPosition());
            }
        });
        holder.todoCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCheck(v.getContext(), holder.getAdapterPosition());
            }
        });

        //Timer setup
        if(todo.isComplete()){
            holder.todoTimerStop.setVisibility(View.GONE);
            holder.todoTimerStart.setVisibility(View.GONE);
            return;
        }

        if(TimerService.mTodoId != todo.getId()){
            Utilities.attachIconPopupMenu(holder.todoTimerStart, R.menu.timer_menu, null, new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Context ctx = holder.itemView.getContext();
                    Todo todo = mTodos.get(holder.getAdapterPosition());
                    switch(item.getItemId()){
                        case R.id.start_pomodoro:
                            Intent serviceIntent = new Intent(ctx, TimerService.class);
                            serviceIntent.setAction(TimerService.ACTION_START_POMODORO);
                            serviceIntent.putExtra(TimerService.EXTRA_TASK_ID, todo.getTaskId());
                            serviceIntent.putExtra(TimerService.EXTRA_TODO_ID, todo.getId());
                            ctx.startService(serviceIntent);
                            break;
                        case R.id.start_timer:
                            Intent startIntent = new Intent(ctx, TimerService.class);
                            startIntent.setAction(TimerService.ACTION_START_TIMER);
                            startIntent.putExtra(TimerService.EXTRA_TASK_ID, todo.getTaskId());
                            startIntent.putExtra(TimerService.EXTRA_TODO_ID, todo.getId());
                            ctx.startService(startIntent);
                            break;
                    }
                    return true;
                }
            });
            holder.todoTimerStart.setVisibility(View.VISIBLE);
            holder.todoTimerStop.setVisibility(View.GONE);
        } else {
            holder.todoTimerStart.setVisibility(View.GONE);
            holder.todoTimerStop.setVisibility(View.VISIBLE);
            holder.todoTimerStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent serviceIntent = new Intent(v.getContext(), TimerService.class);
                    serviceIntent.setAction(TimerService.ACTION_STOP_TIMER);
                    v.getContext().startService(serviceIntent);
                }
            });
        }
    }

    private void showTaskChange(final Context ctx, final int position) {
        final Todo ref = mTodos.get(position);
        final Spinner spinner = new AppCompatSpinner(ctx);
        final List<Task> taskData = DatabaseEditor.getInstance(ctx).getTasks(false);
        int indexOfTask = 0;
        while(true){
            if(indexOfTask == taskData.size()){ //Iff attached to archived task
                indexOfTask = 0;
                break;
            }
            if(taskData.get(indexOfTask).getId() == ref.getId()) break;
            indexOfTask++;
        }
        spinner.setAdapter(new TodoTaskNameAdapter(ctx, -1,taskData));
        spinner.setSelection(indexOfTask);
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.choose_assoc_task)
                .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Task t = (Task) spinner.getSelectedItem();
                        ref.setTaskId(t.getId());
                        DatabaseEditor.getInstance(ctx).updateTodo(ref.getId(), null, -1,
                                null, t.getId(), -1);
                        notifyItemChanged(position);
                    }
                })
                .setView(spinner)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDateChanger(Context ctx, final int position) {
        final Todo ref = mTodos.get(position);
        Date refDate = ref.getDueDateAsDate();
        new DatePickerDialog(ctx, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Date newDate = new Date();
                newDate.setYear(year - 1900);
                newDate.setMonth(month);
                newDate.setDate(dayOfMonth);
                ref.setDate(newDate);
                DatabaseEditor.getInstance(view.getContext()).updateTodo(ref.getId(), null, -1,
                        newDate, -1, -1);
                notifyItemChanged(position);
            }
        }, refDate.getYear() + 1900, refDate.getMonth(), refDate.getDate()).show();
    }

    private void toggleImportance(Context ctx, int position) {
        Todo todo = mTodos.get(position);
        int currentImportance = todo.getImportance();
        int newImportance = 0;
        if(currentImportance < 25){
            newImportance = 25;
        } else if (currentImportance < 50){
            newImportance = 50;
        } else if (currentImportance < 75){
            newImportance = 75;
        } else if (currentImportance < 100){
            newImportance = 100;
        } else {
            newImportance = 0;
        }
        todo.setImportance(newImportance);
        DatabaseEditor.getInstance(ctx).updateTodo(todo.getId(), null, newImportance, null, -1, -1);
        notifyItemChanged(position);
    }

    private void toggleCheck(Context ctx, int position) {
        Todo todo = mTodos.get(position);
        boolean newState = !todo.isComplete();
        DatabaseEditor.getInstance(ctx).updateTodo(todo.getId(), null, -1, null, -1, newState ? 1 : 0);
        todo.setCompleted(newState);
        notifyItemChanged(position); //TODO: Hmm... do we want "completed" tasks to just disappear? This "stay around" behavior might be okay
    }

    public void notifyTimerStart(long taskId, long todoId){
        if(todoId == -1) return; //Nothing for us here, folks

        //There's a to-do timer starting! Search for it -- it probably was started by this very adapter!
        int search = searchForMatchingTimer(todoId);
        if(search != -1){
            mCachedTimerTodoId = todoId;
            mCachedTimerPosition = search;
            notifyItemChanged(search);
        }
    }

    private int searchForMatchingTimer(long todoId) {
        for(int i = 0; i < mTodos.size(); i++){
            if(mTodos.get(i).getId() == todoId){
                return i;
            }
        }
        return -1;
    }

    public void notifyTimerStop(long taskId, long todoId){
        if(mCachedTimerTodoId != -1){
            notifyItemChanged(mCachedTimerPosition);
            mCachedTimerTodoId = -1;
            mCachedTimerPosition = -1;
        }
    }

    @Override
    public int getItemCount() {
        return mTodos.size();
    }
}
