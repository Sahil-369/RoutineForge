package com.routineforge.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    public static String today() {
        return sdf.format(new Date());
    }

    public static String formatDate(long millis) {
        return sdf.format(new Date(millis));
    }

    public static String yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return sdf.format(cal.getTime());
    }

    public static boolean isYesterdayOrToday(String date) {
        return date.equals(today()) || date.equals(yesterday());
    }

    public static long millisUntilNextAlarm(int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar alarm = Calendar.getInstance();
        alarm.set(Calendar.HOUR_OF_DAY, hour);
        alarm.set(Calendar.MINUTE, minute);
        alarm.set(Calendar.SECOND, 0);
        alarm.set(Calendar.MILLISECOND, 0);
        if (alarm.before(now) || alarm.equals(now)) {
            alarm.add(Calendar.DAY_OF_YEAR, 1);
        }
        return alarm.getTimeInMillis();
    }

    public static String getMonthName(int month) {
        String[] months = {"January","February","March","April","May","June",
                "July","August","September","October","November","December"};
        return months[month];
    }

    public static int getDaysInMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getFirstDayOfWeek(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int day = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun
        return day;
    }

    public static String formatDateKey(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    }
}
