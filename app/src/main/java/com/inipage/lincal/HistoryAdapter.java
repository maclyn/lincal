package com.inipage.lincal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryVH> {
    public class HistoryVH extends RecyclerView.ViewHolder {
        TextView date;
        TextView timeRange;
        TextView billed;
        TextView notes;
        TextView taskName;

        public HistoryVH(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.date_string);
            timeRange = (TextView) itemView.findViewById(R.id.time_range);
            billed = (TextView) itemView.findViewById(R.id.billed_footer);
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
        if(r.getTaskCustomerId() != null && !r.getTaskCustomerId().isEmpty()){
            holder.billed.setVisibility(View.VISIBLE);
            holder.billed.setText(r.getBilled() == 1 ? "Billed" : "Not yet billed");
        } else {
            holder.billed.setVisibility(View.GONE);
        }
        if(r.getNote() != null && r.getNote().length() > 0) {
            holder.notes.setVisibility(View.VISIBLE);
            holder.notes.setText(r.getNote());
        } else {
            holder.notes.setVisibility(View.GONE);
        }
        holder.date.setText(dateFormat.format(r.getStartTime()));
        holder.timeRange.setText(timeFormat.format(r.getStartTime()) + " - " + timeFormat.format(r.getEndTime()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context ctx = view.getContext();
                final DatabaseEditor editor = DatabaseEditor.getInstance(view.getContext());
                final Record r = mRecords.get(holder.getAdapterPosition());
                final String note = r.getNote();

                //(2) Add an hourly rate
                final EditText editText = new EditText(view.getContext());
                editText.setText(note);
                editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Edit Note")
                        .setView(editText)
                        .setPositiveButton("Save Note", new DialogInterface.OnClickListener() {
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
