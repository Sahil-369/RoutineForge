package com.routineforge.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.routineforge.models.Task;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "routineforge.db";
    private static final int DB_VERSION = 1;

    // Tasks table
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_TIME = "time";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_DEEP_LINK = "deep_link";
    private static final String COL_ENABLED = "enabled";
    private static final String COL_CREATED_AT = "created_at";

    // Completions table
    private static final String TABLE_COMPLETIONS = "completions";
    private static final String COL_DATE = "date";
    private static final String COL_TASK_ID = "task_id";
    private static final String COL_COMPLETED_AT = "completed_at";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_NAME + " TEXT NOT NULL," +
                COL_TIME + " TEXT NOT NULL," +
                COL_DESCRIPTION + " TEXT," +
                COL_DEEP_LINK + " TEXT," +
                COL_ENABLED + " INTEGER DEFAULT 1," +
                COL_CREATED_AT + " INTEGER" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_COMPLETIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DATE + " TEXT NOT NULL," +
                COL_TASK_ID + " INTEGER NOT NULL," +
                COL_COMPLETED_AT + " INTEGER," +
                "UNIQUE(" + COL_DATE + "," + COL_TASK_ID + ")" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLETIONS);
        onCreate(db);
    }

    // ---- TASKS ----

    public long insertTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, task.getName());
        cv.put(COL_TIME, task.getTime());
        cv.put(COL_DESCRIPTION, task.getDescription());
        cv.put(COL_DEEP_LINK, task.getDeepLink());
        cv.put(COL_ENABLED, task.isEnabled() ? 1 : 0);
        cv.put(COL_CREATED_AT, task.getCreatedAt());
        return db.insert(TABLE_TASKS, null, cv);
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, task.getName());
        cv.put(COL_TIME, task.getTime());
        cv.put(COL_DESCRIPTION, task.getDescription());
        cv.put(COL_DEEP_LINK, task.getDeepLink());
        cv.put(COL_ENABLED, task.isEnabled() ? 1 : 0);
        db.update(TABLE_TASKS, cv, COL_ID + "=?", new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_ID + "=?", new String[]{String.valueOf(taskId)});
        db.delete(TABLE_COMPLETIONS, COL_TASK_ID + "=?", new String[]{String.valueOf(taskId)});
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TASKS, null, null, null, null, null, COL_TIME + " ASC");
        while (c.moveToNext()) {
            Task t = cursorToTask(c);
            tasks.add(t);
        }
        c.close();
        return tasks;
    }

    public Task getTaskById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TASKS, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Task t = null;
        if (c.moveToFirst()) t = cursorToTask(c);
        c.close();
        return t;
    }

    private Task cursorToTask(Cursor c) {
        Task t = new Task();
        t.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        t.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        t.setTime(c.getString(c.getColumnIndexOrThrow(COL_TIME)));
        t.setDescription(c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)));
        t.setDeepLink(c.getString(c.getColumnIndexOrThrow(COL_DEEP_LINK)));
        t.setEnabled(c.getInt(c.getColumnIndexOrThrow(COL_ENABLED)) == 1);
        t.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT)));
        return t;
    }

    // ---- COMPLETIONS ----

    public void markTaskDone(String date, int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DATE, date);
        cv.put(COL_TASK_ID, taskId);
        cv.put(COL_COMPLETED_AT, System.currentTimeMillis());
        db.insertWithOnConflict(TABLE_COMPLETIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void unmarkTaskDone(String date, int taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_COMPLETIONS, COL_DATE + "=? AND " + COL_TASK_ID + "=?",
                new String[]{date, String.valueOf(taskId)});
    }

    public boolean isTaskDone(String date, int taskId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_COMPLETIONS, new String[]{COL_ID},
                COL_DATE + "=? AND " + COL_TASK_ID + "=?",
                new String[]{date, String.valueOf(taskId)}, null, null, null);
        boolean done = c.getCount() > 0;
        c.close();
        return done;
    }

    public int getCompletedCountForDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_COMPLETIONS, new String[]{"COUNT(*) as cnt"},
                COL_DATE + "=?", new String[]{date}, null, null, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public List<String> getDatesWithCompletions() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT " + COL_DATE + " FROM " + TABLE_COMPLETIONS +
                " ORDER BY " + COL_DATE, null);
        while (c.moveToNext()) dates.add(c.getString(0));
        c.close();
        return dates;
    }

    public List<String> getFullyCompletedDates() {
        List<String> dates = new ArrayList<>();
        int totalTasks = getAllTasks().size();
        if (totalTasks == 0) return dates;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_DATE + ", COUNT(*) as cnt FROM " + TABLE_COMPLETIONS +
                " GROUP BY " + COL_DATE + " HAVING cnt >= " + totalTasks, null);
        while (c.moveToNext()) dates.add(c.getString(0));
        c.close();
        return dates;
    }

    public int getPerfectionScore(String date) {
        int total = getAllTasks().size();
        if (total == 0) return 0;
        int done = getCompletedCountForDate(date);
        return (int) ((done / (float) total) * 100);
    }
}
