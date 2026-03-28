package com.routineforge.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.routineforge.database.DatabaseHelper;
import com.routineforge.models.Task;
import com.routineforge.utils.AlarmScheduler;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            AlarmScheduler.createNotificationChannel(context);
            List<Task> tasks = DatabaseHelper.getInstance(context).getAllTasks();
            AlarmScheduler.scheduleAllAlarms(context, tasks);
        }
    }
}
