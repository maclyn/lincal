package com.inipage.lincal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.inipage.lincal.background.ReminderService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.ui.TodoTaskNameAdapter;

import java.util.Date;

public class AddTodoActivity extends AppCompatActivity {
    EditText todoTitle;
    Spinner taskSpinner;
    Button dueDate;
    SeekBar importanceBar;
    Button notImportant;
    Button neutral;
    Button important;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        todoTitle = (EditText) findViewById(R.id.new_todo_title);
        taskSpinner = (Spinner) findViewById(R.id.task_chooser);
        dueDate = (Button) findViewById(R.id.set_due_date);
        importanceBar = (SeekBar) findViewById(R.id.importance_bar);
        notImportant = (Button) findViewById(R.id.set_not_important);
        neutral = (Button) findViewById(R.id.set_neutral);
        important = (Button) findViewById(R.id.set_productive);

        taskSpinner.setAdapter(new TodoTaskNameAdapter(this, -1, DatabaseEditor.getInstance(this).getTasks(false)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_todo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_save:
                if (DatabaseEditor.getInstance(this).addNewTodo(
                        todoTitle.getText().toString(),
                        importanceBar.getProgress(),
                        ((TodoTaskNameAdapter) taskSpinner.getAdapter()).getItem(taskSpinner.getSelectedItemPosition()).getId(),
                        new Date())) {
                    Toast.makeText(this, "Todo added.", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    Toast.makeText(this, "You can't make a task like this.", Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return false;
    }
}
