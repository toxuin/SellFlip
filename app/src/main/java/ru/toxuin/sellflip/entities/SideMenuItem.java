package ru.toxuin.sellflip.entities;

import android.view.View;

public class SideMenuItem {
    private String name;
    private String iconName;
    private View.OnClickListener onClickListener;

    public SideMenuItem(String name, String iconName, View.OnClickListener onClickListener) {
        this.name = name;
        this.iconName = iconName; // prefix with fa for example 'fa-github'
        this.onClickListener = onClickListener;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
