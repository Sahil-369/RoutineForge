package com.routineforge.utils;

import android.content.Context;

import com.routineforge.database.DatabaseHelper;

import java.util.List;

public class StreakManager {

    public static void updateStreak(Context context) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        PrefsManager prefs = PrefsManager.getInstance(context);
        String today = DateUtils.today();
        String yesterday = DateUtils.yesterday();

        int totalTasks = db.getAllTasks().size();
        if (totalTasks == 0) return;

        int todayDone = db.getCompletedCountForDate(today);
        boolean todayComplete = (todayDone >= totalTasks);

        if (!todayComplete) return;

        String lastStreakDate = prefs.getLastStreakDate();
        int currentStreak = prefs.getStreak();

        if (lastStreakDate.equals(today)) return; // Already updated today

        if (lastStreakDate.equals(yesterday)) {
            currentStreak++;
        } else {
            currentStreak = 1;
        }

        prefs.setStreak(currentStreak);
        prefs.setLastStreakDate(today);

        if (currentStreak > prefs.getBestStreak()) {
            prefs.setBestStreak(currentStreak);
        }

        // Update total completed days
        List<String> fullyDone = db.getFullyCompletedDates();
        prefs.setTotalDaysCompleted(fullyDone.size());
    }

    public static int getCurrentStreak(Context context) {
        PrefsManager prefs = PrefsManager.getInstance(context);
        String lastDate = prefs.getLastStreakDate();
        String today = DateUtils.today();
        String yesterday = DateUtils.yesterday();

        // Streak is broken if last date is not today or yesterday
        if (!lastDate.isEmpty() && !lastDate.equals(today) && !lastDate.equals(yesterday)) {
            prefs.setStreak(0);
            prefs.setLastStreakDate("");
            return 0;
        }
        return prefs.getStreak();
    }
}
