package com.routineforge.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.routineforge.R;
import com.routineforge.database.DatabaseHelper;
import com.routineforge.utils.DateUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class CalendarFragment extends Fragment {

    private TextView tvMonthYear;
    private GridLayout gridCalendar;
    private ImageButton btnPrev, btnNext;
    private DatabaseHelper db;
    private int displayYear, displayMonth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        gridCalendar = view.findViewById(R.id.grid_calendar);
        btnPrev = view.findViewById(R.id.btn_prev_month);
        btnNext = view.findViewById(R.id.btn_next_month);

        Calendar cal = Calendar.getInstance();
        displayYear = cal.get(Calendar.YEAR);
        displayMonth = cal.get(Calendar.MONTH);

        btnPrev.setOnClickListener(v -> {
            displayMonth--;
            if (displayMonth < 0) { displayMonth = 11; displayYear--; }
            renderCalendar();
        });

        btnNext.setOnClickListener(v -> {
            displayMonth++;
            if (displayMonth > 11) { displayMonth = 0; displayYear++; }
            renderCalendar();
        });

        renderCalendar();
    }

    private void renderCalendar() {
        tvMonthYear.setText(DateUtils.getMonthName(displayMonth) + " " + displayYear);
        gridCalendar.removeAllViews();

        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            TextView tv = makeDayHeader(d);
            gridCalendar.addView(tv);
        }

        // Get completed dates for this month
        List<String> completedDates = db.getFullyCompletedDates();
        Set<String> completedSet = new HashSet<>(completedDates);

        // Partial completions (any task done)
        List<String> anyDates = db.getDatesWithCompletions();
        Set<String> anySet = new HashSet<>(anyDates);

        String todayStr = DateUtils.today();
        int firstDayOfWeek = DateUtils.getFirstDayOfWeek(displayYear, displayMonth);
        int daysInMonth = DateUtils.getDaysInMonth(displayYear, displayMonth);

        // Empty cells before 1st
        for (int i = 0; i < firstDayOfWeek; i++) {
            View empty = new View(requireContext());
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.height = dpToPx(44);
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            empty.setLayoutParams(lp);
            gridCalendar.addView(empty);
        }

        // Day cells
        for (int day = 1; day <= daysInMonth; day++) {
            String dateKey = DateUtils.formatDateKey(displayYear, displayMonth, day);
            boolean fullyDone = completedSet.contains(dateKey);
            boolean partlyDone = anySet.contains(dateKey);
            boolean isToday = dateKey.equals(todayStr);

            TextView tv = makeDayCell(day, fullyDone, partlyDone, isToday);
            gridCalendar.addView(tv);
        }
    }

    private TextView makeDayHeader(String text) {
        TextView tv = new TextView(requireContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dpToPx(36);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        tv.setLayoutParams(lp);
        tv.setText(text);
        tv.setTextColor(0xFF888888);
        tv.setTextSize(11);
        tv.setGravity(android.view.Gravity.CENTER);
        return tv;
    }

    private TextView makeDayCell(int day, boolean full, boolean partial, boolean isToday) {
        TextView tv = new TextView(requireContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dpToPx(44);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(2, 2, 2, 2);
        tv.setLayoutParams(lp);
        tv.setText(String.valueOf(day));
        tv.setTextSize(13);
        tv.setGravity(android.view.Gravity.CENTER);

        if (full) {
            tv.setBackgroundResource(R.drawable.bg_day_complete);
            tv.setTextColor(0xFFFFFFFF);
        } else if (partial) {
            tv.setBackgroundResource(R.drawable.bg_day_partial);
            tv.setTextColor(0xFFFFFFFF);
        } else if (isToday) {
            tv.setBackgroundResource(R.drawable.bg_day_today);
            tv.setTextColor(0xFFFF6B35);
        } else {
            tv.setTextColor(0xFFCCCCCC);
        }

        return tv;
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        renderCalendar();
    }
}
