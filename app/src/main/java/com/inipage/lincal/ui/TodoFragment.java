package com.inipage.lincal.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.inipage.lincal.AddTaskActivity;
import com.inipage.lincal.AddTodoActivity;
import com.inipage.lincal.R;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;
import com.inipage.lincal.model.Todo;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends Fragment {
    FloatingActionButton addTodo;
    RecyclerView todoView;
    Toolbar toolbar;
    Spinner taskSpinner;

    List<Todo> allTodos;
    List<Task> allTasks;
    Task selectedTask = null;
    boolean showCompleted = false;
    TodoAdapter adapter;

    public TodoFragment(){
    }

    public static TodoFragment newInstance(){
        return new TodoFragment();
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
        View v = inflater.inflate(R.layout.fragment_todo, container, false);

        todoView = (RecyclerView) v.findViewById(R.id.todo_view);
        addTodo = (FloatingActionButton) v.findViewById(R.id.add_todo);
        addTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(allTasks.size() == 1){
                    Toast.makeText(getContext(), R.string.add_task_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(getActivity(), AddTodoActivity.class));
            }
        });
        todoView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayout.VERTICAL));
        toolbar = (Toolbar) v.findViewById(R.id.todo_toolbar);
        toolbar.inflateMenu(R.menu.todo_menu);
        taskSpinner = (Spinner) v.findViewById(R.id.todo_task_spinner);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        setAdapter();
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.show_completed:
                        showCompleted = !item.isChecked();
                        item.setChecked(showCompleted);
                        setAdapter();
                        break;
                    case R.id.mark_all_done:
                        markAllAsDone();
                        break;
                }
                return true;
            }
        });

        allTasks = DatabaseEditor.getInstance(getActivity()).getTasks(false);
        allTasks.add(0, new Task(-1, "All Tasks", "ic_assignment_black_24dp", -1, null, null, -1, -1, false));
        taskSpinner.setOnItemSelectedListener(null);
        taskSpinner.setAdapter(new TaskNameAdapter(getContext(), R.layout.item_task_spinner, allTasks));
        taskSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setAdapter();
            }
        });

        if(selectedTask == null){
            taskSpinner.setSelection(0);
        } else {
            for(int i = 0; i < allTasks.size(); i++){
                if(allTasks.get(i).getId() == selectedTask.getId()){
                    taskSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void markAllAsDone() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.mark_all_done)
                .setMessage(getString(R.string.mark_n_as_done, allTodos.size()))
                .setPositiveButton(R.string.mark_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i < allTodos.size(); i++){
                            Todo todo = allTodos.get(i);
                            if(todo.isComplete()) continue;

                            DatabaseEditor.getInstance(getContext())
                                    .updateTodo(todo.getId(), null, -1, null, -1, 1);
                            todo.setCompleted(true);
                            adapter.notifyItemChanged(i);
                        }

                        Toast.makeText(getContext(), R.string.marked_as_complete, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setAdapter(){
        selectedTask = (Task) taskSpinner.getSelectedItem();
        allTodos = DatabaseEditor.getInstance(getActivity()).getAllTodosSorted(selectedTask == null || selectedTask.getId() == -1 ? null : selectedTask, showCompleted);
        adapter = new TodoAdapter(allTodos, DatabaseEditor.getInstance(getActivity()).getTasks(true), getContext());
        todoView.setLayoutManager(new LinearLayoutManager(getActivity()));
        todoView.setAdapter(adapter);
    }
}
