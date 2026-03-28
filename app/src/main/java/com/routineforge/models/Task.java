package com.routineforge.models;

import java.io.Serializable;

public class Task implements Serializable {
    private int id;
    private String name;
    private String time;
    private String description;
    private String deepLink;
    private boolean enabled;
    private long createdAt;

    public Task() {}

    public Task(String name, String time, String description, String deepLink) {
        this.name = name;
        this.time = time;
        this.description = description;
        this.deepLink = deepLink;
        this.enabled = true;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDeepLink() { return deepLink; }
    public void setDeepLink(String deepLink) { this.deepLink = deepLink; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getHour() {
        if (time == null || !time.contains(":")) return 0;
        try { return Integer.parseInt(time.split(":")[0]); } catch (Exception e) { return 0; }
    }

    public int getMinute() {
        if (time == null || !time.contains(":")) return 0;
        try { return Integer.parseInt(time.split(":")[1]); } catch (Exception e) { return 0; }
    }
}
