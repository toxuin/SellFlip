package ru.toxuin.sellflip.entities;

public class LeftMenuItem {
    private String name;
    private String iconName;

    public LeftMenuItem(String name, String iconName) {
        this.name = name;
        this.iconName = iconName; // prefix with fa for example 'fa-github'
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
}
