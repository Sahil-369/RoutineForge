package com.routineforge.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.routineforge.R;
import com.routineforge.database.DatabaseHelper;
import com.routineforge.utils.PrefsManager;
import com.routineforge.utils.StreakManager;

public class ProfileFragment extends Fragment {

    private TextView tvWhyStarted, tvCurrentStreak, tvBestStreak,
            tvTotalDays, tvTotalTasks, tvTotalTasksAdded;
    private DatabaseHelper db;
    private PrefsManager prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());
        prefs = PrefsManager.getInstance(requireContext());

        tvWhyStarted = view.findViewById(R.id.tv_why_started);
        tvCurrentStreak = view.findViewById(R.id.tv_stat_streak);
        tvBestStreak = view.findViewById(R.id.tv_stat_best_streak);
        tvTotalDays = view.findViewById(R.id.tv_stat_total_days);
        tvTotalTasks = view.findViewById(R.id.tv_stat_tasks_done);
        tvTotalTasksAdded = view.findViewById(R.id.tv_stat_tasks_created);

        loadData();
    }

    private void loadData() {
        String why = prefs.getWhyStarted();
        if (TextUtils.isEmpty(why)) {
            tvWhyStarted.setText("\"Set your intention in Settings\"");
            tvWhyStarted.setAlpha(0.5f);
        } else {
            tvWhyStarted.setText("\"" + why + "\"");
            tvWhyStarted.setAlpha(1f);
        }

        int streak = StreakManager.getCurrentStreak(requireContext());
        tvCurrentStreak.setText(streak + " days");
        tvBestStreak.setText(prefs.getBestStreak() + " days");
        tvTotalDays.setText(String.valueOf(db.getFullyCompletedDates().size()));
        tvTotalTasks.setText(String.valueOf(prefs.getTotalTasksDone()));
        tvTotalTasksAdded.setText(String.valueOf(db.getAllTasks().size()));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
