package com.routineforge.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREFS_NAME = "routineforge_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_WHY_STARTED = "why_started";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_STREAK_DATE = "last_streak_date";
    private static final String KEY_BEST_STREAK = "best_streak";
    private static final String KEY_TOTAL_DAYS_COMPLETED = "total_days_completed";
    private static final String KEY_TOTAL_TASKS_DONE = "total_tasks_done";
    private static final String KEY_NOTIF_CHANNEL_CREATED = "notif_channel_created";

    private final SharedPreferences prefs;

    private static PrefsManager instance;

    public static PrefsManager getInstance(Context ctx) {
        if (instance == null) instance = new PrefsManager(ctx.getApplicationContext());
        return instance;
    }

    private PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingDone() { return prefs.getBoolean(KEY_ONBOARDING_DONE, false); }
    public void setOnboardingDone(boolean done) { prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply(); }

    public String getWhyStarted() { return prefs.getString(KEY_WHY_STARTED, ""); }
    public void setWhyStarted(String why) { prefs.edit().putString(KEY_WHY_STARTED, why).apply(); }

    public int getStreak() { return prefs.getInt(KEY_STREAK, 0); }
    public void setStreak(int streak) { prefs.edit().putInt(KEY_STREAK, streak).apply(); }

    public String getLastStreakDate() { return prefs.getString(KEY_LAST_STREAK_DATE, ""); }
    public void setLastStreakDate(String date) { prefs.edit().putString(KEY_LAST_STREAK_DATE, date).apply(); }

    public int getBestStreak() { return prefs.getInt(KEY_BEST_STREAK, 0); }
    public void setBestStreak(int streak) { prefs.edit().putInt(KEY_BEST_STREAK, streak).apply(); }

    public int getTotalDaysCompleted() { return prefs.getInt(KEY_TOTAL_DAYS_COMPLETED, 0); }
    public void setTotalDaysCompleted(int total) { prefs.edit().putInt(KEY_TOTAL_DAYS_COMPLETED, total).apply(); }

    public int getTotalTasksDone() { return prefs.getInt(KEY_TOTAL_TASKS_DONE, 0); }
    public void incrementTotalTasksDone() {
        prefs.edit().putInt(KEY_TOTAL_TASKS_DONE, getTotalTasksDone() + 1).apply();
    }

    public boolean isNotifChannelCreated() { return prefs.getBoolean(KEY_NOTIF_CHANNEL_CREATED, false); }
    public void setNotifChannelCreated(boolean val) { prefs.edit().putBoolean(KEY_NOTIF_CHANNEL_CREATED, val).apply(); }
}
