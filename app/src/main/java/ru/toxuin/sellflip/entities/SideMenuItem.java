package ru.toxuin.sellflip.entities;

import android.view.View;

public class SideMenuItem {
    private String name;
    private String iconName;
    private int position;
    private View.OnClickListener onClickListener;

    public SideMenuItem(String name, String iconName, View.OnClickListener onClickListener) {
        this.name = name;
        this.iconName = iconName; // prefix with fa for example 'fa-github'
        this.onClickListener = onClickListener;
        this.position = -1;
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

    public void setPosition(int position){
        this.position = position;
    }

    public int getPosition(){
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SideMenuItem)) return false;

        SideMenuItem that = (SideMenuItem) o;

        if (position != that.position) return false;
        if (iconName != null ? !iconName.equals(that.iconName) : that.iconName != null)
            return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (iconName != null ? iconName.hashCode() : 0);
        result = 31 * result + position;
        return result;
    }
}
