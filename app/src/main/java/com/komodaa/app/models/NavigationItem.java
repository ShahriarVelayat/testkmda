package com.komodaa.app.models;

/**
 * Created by nevercom on 1/31/18.
 */

public class NavigationItem {
    private int icon;
    private String title;
    private int id;

    public NavigationItem(int id, String title, int icon) {
        this.icon = icon;
        this.title = title;
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }
}
