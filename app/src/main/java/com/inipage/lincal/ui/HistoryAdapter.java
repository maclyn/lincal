package com.inipage.lincal.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.inipage.lincal.R;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Record;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryVH> {
    public class HistoryVH extends RecyclerView.ViewHolder {
        TextView date;
        TextView timeRange;
        TextView notes;
        TextView taskName;

        public HistoryVH(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.date_string);
            timeRange = (TextView) itemView.findViewById(R.id.time_range);
            notes = (TextView) itemView.findViewById(R.id.notes);
            taskName = (TextView) itemView.findViewById(R.id.task_name);
        }
    }

    List<Record> mRecords;

    public HistoryAdapter(List<Record> records){
        mRecords = records;
    }

    @Override
    public HistoryVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false));
    }

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd");
    static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa");

    @Override
    public void onBindViewHolder(final HistoryVH holder, int position) {
        Record r = mRecords.get(position);
        holder.taskName.setText(r.getTaskName());
        if(r.getNote() != null && r.getNote().length() > 0) {
            holder.notes.setVisibility(View.VISIBLE);
            holder.notes.setText(r.getNote());
        } else {
            holder.notes.setVisibility(View.GONE);
        }

        if(position != 0){
            Record prev = mRecords.get(position - 1);
            Date thisDay = r.getStartTime();
            Date prevEntryDay = prev.getStartTime();

            if(thisDay.getDay() == prevEntryDay.getDay() && thisDay.getMonth() == prevEntryDay.getMonth()
                    && thisDay.getYear() == prevEntryDay.getYear()){
                holder.date.setText("");
            } else {
                holder.date.setText(dateFormat.format(thisDay));
            }
        } else {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText(dateFormat.format(r.getStartTime()));
        }
        holder.timeRange.setText(timeFormat.format(r.getStartTime()) + " - " + timeFormat.format(r.getEndTime()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context ctx = view.getContext();
                final DatabaseEditor editor = DatabaseEditor.getInstance(view.getContext());
                final Record r = mRecords.get(holder.getAdapterPosition());
                final String note = r.getNote();

                //(2) Add an hourly rate
                View dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_edit_note, null);
                final EditText editText = (EditText) dialogView.findViewById(R.id.dialog_edit_note);
                editText.setText(note.equals(ctx.getString(R.string.timed_with_timer)) ? "" : note);
                new AlertDialog.Builder(view.getContext())
                        .setTitle(R.string.edit_note)
                        .setView(dialogView)
                        .setPositiveButton(R.string.save_note, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editor.editNoteForRecord(r.getId(), editText.getText().toString());
                                r.setNote(editText.getText().toString());
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }
}
