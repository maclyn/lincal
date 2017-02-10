package com.inipage.lincal.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.inipage.lincal.AddTaskActivity;
import com.inipage.lincal.R;
import com.inipage.lincal.Utilities;
import com.inipage.lincal.db.DatabaseEditor;
import com.inipage.lincal.model.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskVH> {
    private static final String TAG = "TaskAdapter";

    public class TaskVH extends RecyclerView.ViewHolder {
        FrameLayout tabBackground;
        ImageView icon;
        TextView title;
        TextView subtitle;
        TextView productivity;
        View menu;

        public TaskVH(View itemView) {
            super(itemView);
            tabBackground = (FrameLayout) itemView.findViewById(R.id.task_card_background);
            icon = (ImageView) itemView.findViewById(R.id.task_card_icon);
            title = (TextView) itemView.findViewById(R.id.task_name);
            subtitle = (TextView) itemView.findViewById(R.id.task_details);
            productivity = (TextView) itemView.findViewById(R.id.task_productivity);
            menu = itemView.findViewById(R.id.task_menu_button);
        }
    }

    List<Task> mTasks;
    ReloadListener mListener;

    public interface ReloadListener {
        void onReloadNeeded();
    }

    public TaskAdapter(List<Task> tasks, ReloadListener listener){
        mTasks = tasks;
        mListener = listener;
    }

    @Override
    public TaskVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false));
    }

    @Override
    public void onBindViewHolder(final TaskVH holder, final int position) {
        Task task = mTasks.get(position);

        holder.tabBackground.setBackgroundColor(task.getColor());
        Context ctx = holder.itemView.getContext();
        holder.icon.setImageResource(ctx.getResources().getIdentifier(task.getIcon(), "drawable", ctx.getPackageName()));
        holder.title.setText(task.getName());

        String dowStr = task.getReminderDow();
        int dayCount = (dowStr == null || dowStr.isEmpty() ? 0 : dowStr.trim().split(" ").length);

        if(task.isArchived()){
            holder.subtitle.setText("Archived");
        } else {
            if (dayCount == 0 || task.getReminderThreshold() == 0) {
                holder.subtitle.setText("No reminder set");
            } else {
                holder.subtitle.setText("Goal: " + task.getReminderThreshold() + " minutes, " + dayCount + "x/week");
            }
        }

        String productivity = "Neither productive nor unproductive";
        if(task.getProductivityLevel() < 26){
            productivity = "Very unproductive";
        } else if (task.getProductivityLevel() < 45){
            productivity = "Slightly unproductive";
        } else if (task.getProductivityLevel() > 55){
            if(task.getProductivityLevel() > 74){
                productivity = "Very productive";
            } else {
                productivity = "Slightly productive";
            }
        }
        holder.productivity.setText(productivity);
        Utilities.attachIconPopupMenu(holder.menu, R.menu.context_task,
                new Utilities.MenuPrepListener() {
                    @Override
                    public void onPrepMenu(Menu menu) {
                        menu.findItem(R.id.archive_task).setTitle(
                                mTasks.get(holder.getAdapterPosition()).isArchived() ?
                                        R.string.unarchive :
                                        R.string.archive);
                    }
                },
                new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch(item.getItemId()){
                        case R.id.edit_task:
                            Intent editIntent = new Intent(holder.itemView.getContext(), AddTaskActivity.class);
                            editIntent.putExtra(AddTaskActivity.EXTRA_TASK_TO_EDIT_ID, mTasks.get(holder.getAdapterPosition()).getId());
                            holder.itemView.getContext().startActivity(editIntent);
                            break;
                        case R.id.delete_task:
                            deleteItem(holder.itemView.getContext(), holder.getAdapterPosition());
                            break;
                        case R.id.archive_task:
                            toggleArchived(holder.itemView.getContext(), holder.getAdapterPosition());
                            break;
                    }
                    return true;
                }
            });
    }

    private void toggleArchived(Context ctx, int position) {
        Task t = mTasks.get(position);
        DatabaseEditor.getInstance(ctx).updateTask(t.getId(), null, null, -1, null, null, -1, -1,
                t.isArchived() ? 0 : 1);
        mListener.onReloadNeeded();
    }

    private void deleteItem(Context ctx, final int position){
        Utilities.showConfirmationDialog(ctx, R.string.delete_task_title, R.string.delete_task_msg,
                R.string.delete, R.string.no, new Utilities.ConfirmationListener() {
                    @Override
                    public void onConfirmed(Context ctx) {
                        DatabaseEditor.getInstance(ctx).deleteTask(mTasks.get(position).getId());
                        mTasks.remove(position);
                        notifyItemRemoved(position);
                    }

                    @Override
                    public void onRejected(Context ctx) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }
}
