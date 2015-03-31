package ru.toxuin.sellflip.entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Category {
    @SerializedName("_id")
    String id;
    String name;
    List subcategories;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List getSubcategories() {
        return subcategories;
    }

    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    public boolean contains(Category other) {
        return subcategories != null && subcategories.contains(other);
    }

    public static class List extends ArrayList<Category> {}
}
