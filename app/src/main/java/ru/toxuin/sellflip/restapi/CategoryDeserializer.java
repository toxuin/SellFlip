package ru.toxuin.sellflip.restapi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import ru.toxuin.sellflip.entities.Category;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CategoryDeserializer implements JsonDeserializer<Category.List> {
    private static final String TAG = "CATEG_DESERIALIZER";
    private Gson gson = new Gson();

    @Override
    public Category.List deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Category.List resultList = new Category.List();
        Map<String, Category> categories = new HashMap<>();
        JsonArray value = json.getAsJsonArray();
        if (value != null) {
            for (JsonElement element : value) {
                Category categ = gson.fromJson(element, Category.class);
                categories.put(categ.getId(), categ);
                if (categ.getParent() == null) resultList.add(categ);
            }

            for (Map.Entry<String, Category> entry : categories.entrySet()) {
                Category categ = entry.getValue();
                if (categories.containsKey(categ.getParent())) {
                    categories.get(categ.getParent()).addSubcategory(categ);
                }
            }
        }

        return resultList;
    }
}
