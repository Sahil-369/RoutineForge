package com.routineforge.fragments;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.routineforge.R;
import com.routineforge.database.DatabaseHelper;
import com.routineforge.utils.AlarmScheduler;
import com.routineforge.utils.PrefsManager;
import com.routineforge.utils.StreakManager;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_CODE_PICK_SOUND = 1001;

    private TextView tvWhyStarted, tvCurrentStreak, tvBestStreak,
            tvTotalDays, tvTotalTasks, tvTotalTasksAdded;
    private TextView tvCustomSoundTitle;
    private Button btnChooseSound;
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
        tvCustomSoundTitle = view.findViewById(R.id.tv_custom_sound_title);
        btnChooseSound = view.findViewById(R.id.btn_choose_sound);

        btnChooseSound.setOnClickListener(v -> openRingtonePicker());

        loadData();
        loadCustomSound();
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

    private void loadCustomSound() {
        String soundUri = prefs.getCustomNotificationSound();
        if (TextUtils.isEmpty(soundUri)) {
            tvCustomSoundTitle.setText("Default sound");
            return;
        }

        Uri uri = Uri.parse(soundUri);
        try {
            if (RingtoneManager.getRingtone(requireContext(), uri) != null) {
                String title = RingtoneManager.getRingtone(requireContext(), uri).getTitle(requireContext());
                tvCustomSoundTitle.setText(title);
            } else {
                tvCustomSoundTitle.setText("Default sound");
            }
        } catch (Exception ignored) {
            tvCustomSoundTitle.setText("Default sound");
        }
    }

    private void openRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select notification sound");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);

        String currentSound = prefs.getCustomNotificationSound();
        if (!TextUtils.isEmpty(currentSound)) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(currentSound));
        } else {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_SOUND);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_SOUND && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                prefs.setCustomNotificationSound(uri.toString());
                tvCustomSoundTitle.setText(RingtoneManager.getRingtone(requireContext(), uri).getTitle(requireContext()));
            } else {
                prefs.setCustomNotificationSound("");
                tvCustomSoundTitle.setText("Default sound");
            }
            AlarmScheduler.createNotificationChannel(requireContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
