package com.routineforge.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.routineforge.R;
import com.routineforge.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskListener {
        void onDone(Task task, boolean done);
        void onEdit(Task task);
        void onDelete(Task task);
        void onDeepLink(Task task);
    }

    private List<Task> tasks;
    private java.util.Set<Integer> doneTaskIds;
    private TaskListener listener;
    private Context context;

    public TaskAdapter(Context context, List<Task> tasks, java.util.Set<Integer> doneTaskIds, TaskListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.doneTaskIds = doneTaskIds;
        this.listener = listener;
    }

    public void updateTasks(List<Task> tasks, java.util.Set<Integer> doneTaskIds) {
        this.tasks = tasks;
        this.doneTaskIds = doneTaskIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder h, int position) {
        Task task = tasks.get(position);
        boolean done = doneTaskIds.contains(task.getId());

        h.tvTime.setText(task.getTime());
        h.tvName.setText(task.getName());

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            h.tvDescription.setVisibility(View.VISIBLE);
            h.tvDescription.setText(task.getDescription());
        } else {
            h.tvDescription.setVisibility(View.GONE);
        }

        // Strike through if done
        if (done) {
            h.tvName.setPaintFlags(h.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(0.5f);
            h.tvTime.setAlpha(0.5f);
            h.btnDone.setImageResource(R.drawable.ic_done_filled);
            h.cardContainer.setAlpha(0.7f);
        } else {
            h.tvName.setPaintFlags(h.tvName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            h.tvName.setAlpha(1f);
            h.tvTime.setAlpha(1f);
            h.btnDone.setImageResource(R.drawable.ic_done_outline);
            h.cardContainer.setAlpha(1f);
        }

        // Deep link button visibility
        boolean hasDeepLink = task.getDeepLink() != null && !task.getDeepLink().isEmpty();
        h.btnStart.setVisibility(hasDeepLink ? View.VISIBLE : View.GONE);

        h.btnDone.setOnClickListener(v -> {
            if (listener != null) listener.onDone(task, !done);
        });

        h.btnStart.setOnClickListener(v -> {
            if (listener != null) listener.onDeepLink(task);
        });

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(task);
        });

        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(task);
        });
    }

    @Override
    public int getItemCount() { return tasks.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvName, tvDescription;
        ImageButton btnDone, btnStart, btnEdit, btnDelete;
        View cardContainer;

        TaskViewHolder(View v) {
            super(v);
            tvTime = v.findViewById(R.id.tv_task_time);
            tvName = v.findViewById(R.id.tv_task_name);
            tvDescription = v.findViewById(R.id.tv_task_description);
            btnDone = v.findViewById(R.id.btn_task_done);
            btnStart = v.findViewById(R.id.btn_task_start);
            btnEdit = v.findViewById(R.id.btn_task_edit);
            btnDelete = v.findViewById(R.id.btn_task_delete);
            cardContainer = v.findViewById(R.id.card_task_container);
        }
    }
}
