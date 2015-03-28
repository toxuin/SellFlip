package ru.toxuin.sellflip.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Category {
    @SerializedName("_id")
    String id;
    String name;
    List<Category> subcategories;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Category> getSubcategories() {
        return subcategories;
    }

    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    public boolean contains(Category other) {
        return subcategories != null && subcategories.contains(other);
    }
}
