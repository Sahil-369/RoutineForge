package com.routineforge;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.routineforge.database.DatabaseHelper;
import com.routineforge.models.Task;
import com.routineforge.utils.AlarmScheduler;

import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "extra_task";
    private Task editingTask = null;

    private TextView tvTitle;
    private EditText etName, etDescription, etDeepLink;
    private Button btnTimePicker, btnSave;
    private ImageButton btnBack;
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        tvTitle = findViewById(R.id.tv_add_edit_title);
        etName = findViewById(R.id.et_task_name);
        etDescription = findViewById(R.id.et_task_description);
        etDeepLink = findViewById(R.id.et_task_deeplink);
        btnTimePicker = findViewById(R.id.btn_time_picker);
        btnSave = findViewById(R.id.btn_save_task);
        btnBack = findViewById(R.id.btn_back);

        editingTask = (Task) getIntent().getSerializableExtra(EXTRA_TASK);

        if (editingTask != null) {
            tvTitle.setText("Edit Task");
            etName.setText(editingTask.getName());
            etDescription.setText(editingTask.getDescription());
            etDeepLink.setText(editingTask.getDeepLink());
            selectedTime = editingTask.getTime();
            btnTimePicker.setText(selectedTime);
            btnSave.setText("Update Task");
        } else {
            tvTitle.setText("New Task");
            // Default to current time
            Calendar cal = Calendar.getInstance();
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d",
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            btnTimePicker.setText(selectedTime);
        }

        btnBack.setOnClickListener(v -> finish());

        btnTimePicker.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> saveTask());
    }

    private void showTimePicker() {
        String[] parts = selectedTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            btnTimePicker.setText(selectedTime);
        }, hour, minute, true);
        dialog.show();
    }

    private void saveTask() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Task name required");
            return;
        }
        if (TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim();
        String deepLink = etDeepLink.getText().toString().trim();

        DatabaseHelper db = DatabaseHelper.getInstance(this);

        if (editingTask != null) {
            AlarmScheduler.cancelAlarm(this, editingTask.getId());
            editingTask.setName(name);
            editingTask.setTime(selectedTime);
            editingTask.setDescription(description);
            editingTask.setDeepLink(deepLink);
            db.updateTask(editingTask);
            AlarmScheduler.scheduleAlarm(this, editingTask);
            Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
        } else {
            Task task = new Task(name, selectedTime, description, deepLink);
            long id = db.insertTask(task);
            task.setId((int) id);
            AlarmScheduler.scheduleAlarm(this, task);
            Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}
