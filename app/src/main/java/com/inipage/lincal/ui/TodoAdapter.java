package com.inipage.lincal.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inipage.lincal.R;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Todo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoVH> {
    public class TodoVH extends RecyclerView.ViewHolder {
        TextView todoTitle;
        ImageView todoCheck;
        View todoTimerStart;
        View todoTimerStop;
        View todoDueDateButton;
        View todoImportanceButton;
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
            todoTaskIcon = (ImageView) itemView.findViewById(R.id.todo_task);
            todoTimerStart = itemView.findViewById(R.id.todo_timer);
            todoTimerStop = itemView.findViewById(R.id.todo_stop);
        }
    }

    List<Todo> mTodos;

    public TodoAdapter(List<Todo> tasks){
        mTodos = tasks;
    }

    @Override
    public TodoVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TodoVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false));
    }

    private final SimpleDateFormat SHORT_DATE = new SimpleDateFormat("M/dd", Locale.US);

    @Override
    public void onBindViewHolder(final TodoVH holder, int position) {
        Todo todo = mTodos.get(position);
        holder.todoTitle.setText(todo.getTitle());
        holder.todoCheck.setImageResource(todo.isComplete() ? R.drawable.ic_check_box_black_48dp :
            R.drawable.ic_check_box_outline_blank_black_48dp);
        holder.todoTimerStop.setVisibility(View.GONE);
        holder.todoDudeDateText.setText(SHORT_DATE.format(todo.getDueDateAsDate()));
        holder.todoImportanceText.setText(String.valueOf(todo.getImportance()));

        holder.todoDueDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.todoCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCheck(v.getContext(), holder.getAdapterPosition());
            }
        });
    }

    private void toggleCheck(Context ctx, int position) {
        Todo todo = mTodos.get(position);
        boolean newState = !todo.isComplete();
        DatabaseEditor.getInstance(ctx).updateTodo(todo.getId(), null, -1, null, -1, newState ? 1 : 0);
        todo.setCompleted(newState);
        notifyItemChanged(position); //TODO: Hmm... do we want "completed" tasks to just disappear? This "stay around" behavior might be okay
    }

    @Override
    public int getItemCount() {
        return mTodos.size();
    }
}
