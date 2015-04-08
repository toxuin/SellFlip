package ru.toxuin.sellflip.entities;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("_id")
    private String id;

    private String name;

    private String role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
