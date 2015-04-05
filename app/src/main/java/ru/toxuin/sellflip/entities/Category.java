package ru.toxuin.sellflip.entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Category {
    @SerializedName("_id")
    String id;
    String name;
    String parent;
    String[] subcategories;
    private List subcats = new List();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List getSubcategories() {
        return subcats;
    }

    public boolean hasSubcategories() {
        return subcats != null && !subcats.isEmpty();
    }

    public boolean contains(Category other) {
        return subcats != null && subcats.contains(other);
    }

    public void addSubcategory(Category kid) {
        this.subcats.add(kid);
    }

    public String[] getSubcategoryIds() {
        return subcategories;
    }

    public String getParent() {
        return parent;
    }

    public static class List extends ArrayList<Category> {}
}
