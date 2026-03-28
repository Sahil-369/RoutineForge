package com.routineforge;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.routineforge.utils.AlarmScheduler;
import com.routineforge.utils.PrefsManager;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        AlarmScheduler.createNotificationChannel(this);

        EditText etWhy = findViewById(R.id.et_why_started);
        Button btnStart = findViewById(R.id.btn_get_started);
        TextView tvSkip = findViewById(R.id.tv_skip);

        btnStart.setOnClickListener(v -> {
            String why = etWhy.getText().toString().trim();
            PrefsManager prefs = PrefsManager.getInstance(this);
            prefs.setWhyStarted(why);
            prefs.setOnboardingDone(true);
            startMain();
        });

        tvSkip.setOnClickListener(v -> {
            PrefsManager.getInstance(this).setOnboardingDone(true);
            startMain();
        });
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
