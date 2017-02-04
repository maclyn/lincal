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

import com.reimaginebanking.api.nessieandroidsdk.NessieError;
import com.reimaginebanking.api.nessieandroidsdk.NessieResultsListener;
import com.reimaginebanking.api.nessieandroidsdk.constants.BillStatus;
import com.reimaginebanking.api.nessieandroidsdk.models.Account;
import com.reimaginebanking.api.nessieandroidsdk.models.Bill;
import com.reimaginebanking.api.nessieandroidsdk.requestclients.NessieClient;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Maclyn on 2/4/2017.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {
    private static final String TAG = "TaskAdapter";

    public class TaskVH extends RecyclerView.ViewHolder {
        FrameLayout tabBackground;
        ImageView icon;
        TextView title;
        TextView subtitle;
        Button bill;

        public TaskVH(View itemView) {
            super(itemView);
            tabBackground = (FrameLayout) itemView.findViewById(R.id.task_card_background);
            icon = (ImageView) itemView.findViewById(R.id.task_card_icon);
            title = (TextView) itemView.findViewById(R.id.task_name);
            subtitle = (TextView) itemView.findViewById(R.id.task_details);
            bill = (Button) itemView.findViewById(R.id.bill_button);
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

        if(!mTasks.get(position).getCustomerId().isEmpty()){
            holder.bill.setVisibility(View.VISIBLE);
            holder.bill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //(1) Fetch total "unbilled" time
                    final Context ctx = view.getContext();
                    final DatabaseEditor editor = DatabaseEditor.getInstance(view.getContext());
                    final Task task = mTasks.get(holder.getAdapterPosition());
                    final long unbilledSeconds = editor.getUnbilledTimeForTask(task.getId());
                    final String invoice = editor.getUnbilledNotesForTask(task.getId());

                    //(2) Add an hourly rate
                    final EditText editText = new EditText(view.getContext());
                    editText.setHint("Enter your hourly rate");
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Billing for " + unbilledSeconds + " seconds of work")
                            .setView(editText)
                            .setPositiveButton("Prepare Invoice", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    double hourlyRate = editText.getText().toString().isEmpty() ? 10.0d : Double.valueOf(editText.getText().toString());
                                    final Double amount = ((double) unbilledSeconds) / 60D / 60D * hourlyRate;
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    final String finalMsg = "Bill for " + unbilledSeconds + " seconds of work @ " + hourlyRate + "/hr = $" + df.format(amount) + "\n\n" + invoice;
                                    new AlertDialog.Builder(ctx)
                                            .setTitle("Preview Invoice")
                                            .setMessage(finalMsg)
                                            .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //(3) Fetch customer accounts
                                                    final NessieClient client = NessieClient.getInstance("1e8703bbc020650c211be2d253a46834");
                                                    client.ACCOUNT.getCustomerAccounts(task.getCustomerId(), new NessieResultsListener() {
                                                        @Override
                                                        public void onSuccess(Object result) {
                                                            List<Account> accounts = (List<Account>) result;
                                                            String id = null;
                                                            for(Account a : accounts){
                                                                Log.d(TAG, a.getBalance() + " " + a.getId());
                                                                id = a.getId();
                                                            }

                                                            Log.d(TAG, "Billing for " + amount);

                                                            client.BILL.createBill(id, new Bill.Builder()
                                                                    .nickname(finalMsg)
                                                                    .payee(task.getCustomerId())
                                                                    .paymentAmount(amount)
                                                                    .recurringDate(1)
                                                                    .paymentDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                                                                    .status(BillStatus.PENDING)
                                                                    .build(), new NessieResultsListener() {
                                                                @Override
                                                                public void onSuccess(Object result) {
                                                                    editor.markTaskBilled(task.getId());
                                                                    Toast.makeText(ctx, "Bill successfully sent.", Toast.LENGTH_SHORT).show();
                                                                }

                                                                @Override
                                                                public void onFailure(NessieError error) {
                                                                    Log.d(TAG, "Error: " + error.getMessage());
                                                                    Toast.makeText(ctx, ":(", Toast.LENGTH_SHORT);
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(NessieError error) {
                                                            Log.d(TAG, "Error: " + error.getMessage());
                                                            Toast.makeText(ctx, ":(", Toast.LENGTH_SHORT);
                                                        }
                                                    });
                                                }
                                            })
                                            .show();
                                }
                            })
                            .show();
                }
            });
        } else {
            holder.bill.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }
}
