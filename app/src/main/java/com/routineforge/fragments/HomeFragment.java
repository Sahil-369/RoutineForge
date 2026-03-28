package com.routineforge.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.routineforge.AddEditTaskActivity;
import com.routineforge.R;
import com.routineforge.adapters.TaskAdapter;
import com.routineforge.database.DatabaseHelper;
import com.routineforge.models.Task;
import com.routineforge.utils.DateUtils;
import com.routineforge.utils.PrefsManager;
import com.routineforge.utils.StreakManager;
import com.routineforge.views.CircularProgressView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment implements TaskAdapter.TaskListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private FloatingActionButton fabAdd;
    private TextView tvDate, tvGreeting, tvStreak, tvEmpty;
    private CircularProgressView progressView;
    private ImageView ivFlame;
    private DatabaseHelper db;
    private PrefsManager prefs;
    private String today;

    private ActivityResultLauncher<Intent> addEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { if (result.getResultCode() == android.app.Activity.RESULT_OK) refresh(); });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        prefs = PrefsManager.getInstance(requireContext());
        today = DateUtils.today();

        tvDate = view.findViewById(R.id.tv_today_date);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvStreak = view.findViewById(R.id.tv_streak_count);
        tvEmpty = view.findViewById(R.id.tv_empty);
        progressView = view.findViewById(R.id.circular_progress);
        ivFlame = view.findViewById(R.id.iv_flame);
        fabAdd = view.findViewById(R.id.fab_add_task);

        recyclerView = view.findViewById(R.id.recycler_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditTaskActivity.class);
            addEditLauncher.launch(intent);
        });

        refresh();
    }

    public void refresh() {
        if (!isAdded()) return;
        today = DateUtils.today();

        // Date + greeting
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) greeting = "Good Morning ☀️";
        else if (hour < 17) greeting = "Good Afternoon 🌤";
        else greeting = "Good Evening 🌙";
        tvGreeting.setText(greeting);

        // Tasks
        List<Task> tasks = db.getAllTasks();
        Set<Integer> doneIds = new HashSet<>();
        for (Task t : tasks) {
            if (db.isTaskDone(today, t.getId())) doneIds.add(t.getId());
        }

        if (tasks.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new TaskAdapter(requireContext(), tasks, doneIds, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateTasks(tasks, doneIds);
        }

        // Progress
        int score = db.getPerfectionScore(today);
        progressView.setProgress(score);

        // Streak
        StreakManager.updateStreak(requireContext());
        int streak = StreakManager.getCurrentStreak(requireContext());
        tvStreak.setText(String.valueOf(streak));
    }

    @Override
    public void onDone(Task task, boolean done) {
        if (done) {
            db.markTaskDone(today, task.getId());
            prefs.incrementTotalTasksDone();
        } else {
            db.unmarkTaskDone(today, task.getId());
        }
        StreakManager.updateStreak(requireContext());
        refresh();
    }

    @Override
    public void onEdit(Task task) {
        Intent intent = new Intent(requireContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskActivity.EXTRA_TASK, task);
        addEditLauncher.launch(intent);
    }

    @Override
    public void onDelete(Task task) {
        new AlertDialog.Builder(requireContext(), R.style.DarkAlertDialog)
                .setTitle("Delete Task")
                .setMessage("Delete \"" + task.getName() + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    com.routineforge.utils.AlarmScheduler.cancelAlarm(requireContext(), task.getId());
                    db.deleteTask(task.getId());
                    refresh();
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeepLink(Task task) {
        String link = task.getDeepLink();
        if (link == null || link.isEmpty()) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Cannot open link: " + link, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}
