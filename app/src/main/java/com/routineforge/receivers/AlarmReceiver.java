package com.routineforge.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.routineforge.utils.PrefsManager;

import com.routineforge.MainActivity;
import com.routineforge.R;
import com.routineforge.database.DatabaseHelper;
import com.routineforge.models.Task;
import com.routineforge.utils.AlarmScheduler;
import com.routineforge.utils.DateUtils;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, -1);
        String taskName = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_NAME);
        String deepLink = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_DEEPLINK);

        if (taskId == -1) return;

        // Wake screen
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "RoutineForge::AlarmWakeLock");
        wl.acquire(3000);

        // Reschedule for next day
        Task task = DatabaseHelper.getInstance(context).getTaskById(taskId);
        if (task != null && task.isEnabled()) {
            AlarmScheduler.scheduleAlarm(context, task);
        }

        showNotification(context, taskId, taskName, deepLink);
    }

    private void showNotification(Context context, int taskId, String taskName, String deepLink) {
        AlarmScheduler.createNotificationChannel(context);

        // Main tap intent → open app
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.putExtra("task_id", taskId);
        PendingIntent mainPi = PendingIntent.getActivity(
                context, taskId * 10, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Done action
        Intent doneIntent = new Intent(context, DoneReceiver.class);
        doneIntent.putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId);
        doneIntent.putExtra("date", DateUtils.today());
        PendingIntent donePi = PendingIntent.getBroadcast(
                context, taskId * 10 + 1, doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Snooze action (10 min)
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId);
        snoozeIntent.putExtra(AlarmScheduler.EXTRA_TASK_NAME, taskName);
        snoozeIntent.putExtra(AlarmScheduler.EXTRA_TASK_DEEPLINK, deepLink);
        PendingIntent snoozePi = PendingIntent.getBroadcast(
                context, taskId * 10 + 2, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, AlarmScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ " + taskName)
                .setContentText("Time to work! Tap Done when finished.")
                .setColor(0xFF6B35)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(mainPi)
                .addAction(R.drawable.ic_done, "✓ Done", donePi)
                .addAction(R.drawable.ic_snooze, "⏰ Snooze 10m", snoozePi)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        String soundUri = PrefsManager.getInstance(context).getCustomNotificationSound();
        if (soundUri != null && !soundUri.isEmpty()) {
            builder.setSound(Uri.parse(soundUri));
        } else {
            builder.setDefaults(Notification.DEFAULT_ALL);
        }

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(taskId, builder.build());
    }
}
