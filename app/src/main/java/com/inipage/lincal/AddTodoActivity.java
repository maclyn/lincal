package com.inipage.lincal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.inipage.lincal.background.ReminderService;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.ui.TodoTaskNameAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
    EditText todoTitle;
    Spinner taskSpinner;
    Button setDueDate;
    SeekBar importanceBar;
    Button notImportant;
    Button neutral;
    Button important;

    Date dueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        todoTitle = (EditText) findViewById(R.id.new_todo_title);
        taskSpinner = (Spinner) findViewById(R.id.task_chooser);
        setDueDate = (Button) findViewById(R.id.set_due_date);
        importanceBar = (SeekBar) findViewById(R.id.importance_bar);
        notImportant = (Button) findViewById(R.id.set_not_important);
        neutral = (Button) findViewById(R.id.set_neutral);
        important = (Button) findViewById(R.id.set_important);

        taskSpinner.setAdapter(new TodoTaskNameAdapter(this, -1, DatabaseEditor.getInstance(this).getTasks(false)));
        notImportant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importanceBar.setProgress(0);
            }
        });
        neutral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importanceBar.setProgress(50);
            }
        });
        important.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importanceBar.setProgress(100);
            }
        });

        Calendar today = new GregorianCalendar();
        today.roll(Calendar.WEEK_OF_YEAR, 1);
        dueDate = today.getTime();
        setDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDueDatePicker();
            }
        });
        setDueDateText();
    }

    private void showDueDatePicker() {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Date newDate = new Date();
                newDate.setYear(year - 1900);
                newDate.setMonth(month);
                newDate.setDate(dayOfMonth);
                dueDate = newDate;
                setDueDateText();
            }
        }, dueDate.getYear() + 1900, dueDate.getMonth(), dueDate.getDate()).show();
    }

    private final SimpleDateFormat DATE_SDF = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
    private void setDueDateText(){
        setDueDate.setText(DATE_SDF.format(dueDate));
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
                        dueDate)) {
                    Toast.makeText(this, "Todo added.", Toast.LENGTH_SHORT).show();

                    finish();
                } else {
                    Toast.makeText(this, "You can't make a todo like this.", Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return false;
    }
}
