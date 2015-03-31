package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.restapi.ApiService;

public class CategoryRequest extends RetrofitSpiceRequest<Category.List, ApiService> {
    private static final String TAG = "CATEGORY_REQUEST";

    public CategoryRequest() {
        super(Category.List.class, ApiService.class);
    }

    @Override
    public Category.List loadDataFromNetwork() throws Exception {
        return getService().getCategories();
    }

    public static String getCacheKey() {
        return "categories";
    }
}
