package com.routineforge.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.routineforge.utils.AlarmScheduler;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1);
        String taskName = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_NAME);
        String deepLink = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_DEEPLINK);
        if (taskId == -1) return;

        // Dismiss notification
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(taskId);

        // Schedule snooze in 10 minutes
        long snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId);
        alarmIntent.putExtra(AlarmScheduler.EXTRA_TASK_NAME, taskName);
        alarmIntent.putExtra(AlarmScheduler.EXTRA_TASK_DEEPLINK, deepLink);

        PendingIntent pi = PendingIntent.getBroadcast(
                context, taskId * 100, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
                }
            } catch (SecurityException e) {
                am.set(AlarmManager.RTC_WAKEUP, snoozeTime, pi);
            }
        }
    }
}
