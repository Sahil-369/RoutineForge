package com.routineforge.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import com.routineforge.models.Task;
import com.routineforge.receivers.AlarmReceiver;
import com.routineforge.utils.PrefsManager;

import java.util.List;

public class AlarmScheduler {

    public static final String CHANNEL_ID = "routineforge_tasks";
    public static final String CHANNEL_NAME = "RoutineForge Tasks";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_NAME = "task_name";
    public static final String EXTRA_TASK_DEEPLINK = "task_deeplink";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm == null) return;

            // Recreate channel to apply latest custom sound settings on supported devices.
            if (nm.getNotificationChannel(CHANNEL_ID) != null) {
                nm.deleteNotificationChannel(CHANNEL_ID);
            }

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Daily task reminders");
            channel.enableVibration(true);
            channel.setShowBadge(true);

            String soundUri = PrefsManager.getInstance(context).getCustomNotificationSound();
            if (soundUri != null && !soundUri.isEmpty()) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(Uri.parse(soundUri), audioAttributes);
            }

            nm.createNotificationChannel(channel);
            PrefsManager.getInstance(context).setNotifChannelCreated(true);
        }
    }

    public static void scheduleAlarm(Context context, Task task) {
        if (!task.isEnabled()) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_TASK_ID, task.getId());
        intent.putExtra(EXTRA_TASK_NAME, task.getName());
        intent.putExtra(EXTRA_TASK_DEEPLINK, task.getDeepLink());

        PendingIntent pi = PendingIntent.getBroadcast(
                context, task.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerAt = DateUtils.millisUntilNextAlarm(task.getHour(), task.getMinute());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            }
        } catch (SecurityException e) {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, taskId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
    }

    public static void scheduleAllAlarms(Context context, List<Task> tasks) {
        for (Task task : tasks) {
            if (task.isEnabled()) scheduleAlarm(context, task);
        }
    }

    public static void cancelAllAlarms(Context context, List<Task> tasks) {
        for (Task task : tasks) {
            cancelAlarm(context, task.getId());
        }
    }
}
