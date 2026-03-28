package com.routineforge.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.routineforge.database.DatabaseHelper;
import com.routineforge.utils.AlarmScheduler;
import com.routineforge.utils.PrefsManager;
import com.routineforge.utils.StreakManager;

public class DoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1);
        String date = intent.getStringExtra("date");
        if (taskId == -1 || date == null) return;

        DatabaseHelper.getInstance(context).markTaskDone(date, taskId);
        PrefsManager.getInstance(context).incrementTotalTasksDone();
        StreakManager.updateStreak(context);

        // Dismiss notification
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(taskId);
    }
}
