package ru.toxuin.sellflip.restapi.spicerequests;

import android.util.Log;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.restapi.ApiService;

public class ListAdsRequest extends RetrofitSpiceRequest<SingleAd.List, ApiService> {
    private static final String TAG = "LIST_ADS_REQUEST";
    private String category;
    private String searchTerm;
    private int page;

    private static int itemsPerPage = 7;

    public ListAdsRequest(String category, String searchTerm, int page) {
        super(SingleAd.List.class, ApiService.class);
        this.searchTerm = searchTerm;
        this.category = category;
        this.page = page;
    }

    @Override
    public SingleAd.List loadDataFromNetwork() throws Exception {
        int skip = itemsPerPage * page;
        int limit = itemsPerPage;
        Log.d(TAG, "REQUESTING TOP ADS FROM " + skip + " TO " + (skip + limit) + "(" + limit + " ITEMS)");
        return getService().listTopAds(category, searchTerm, skip, limit);
    }

    public String getCacheKey() {
        return "getTopAds." + category + "." + page;
    }

    public static void setItemsPerPage(int perPage) {
        itemsPerPage = perPage;
    }

    public static int getItemsPerPage() {
        return itemsPerPage;
    }
}
