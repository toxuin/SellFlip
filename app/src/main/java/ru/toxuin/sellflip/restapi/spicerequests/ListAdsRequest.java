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
    private Boolean trendingOrder;

    private static int itemsPerPage = 7;

    public ListAdsRequest(String category, String searchTerm, Boolean trendingOrder, int page) {
        super(SingleAd.List.class, ApiService.class);
        this.searchTerm = searchTerm;
        this.category = category;
        this.trendingOrder = trendingOrder;
        this.page = page;
    }

    @Override
    public SingleAd.List loadDataFromNetwork() throws Exception {
        int skip = itemsPerPage * page;
        int limit = itemsPerPage;
        Log.d(TAG, "REQUESTING TOP ADS FROM " + skip + " TO " + (skip + limit) + "(" + limit + " ITEMS)");
        return getService().listTopAds(category, searchTerm, trendingOrder, skip, limit);
    }

    public String getCacheKey() {
        String trend = ".trendy";
        if (trendingOrder == null || trendingOrder.equals(Boolean.FALSE)) trend = "";
        return "getTopAds." + category + "." + page + trend;
    }

    public static void setItemsPerPage(int perPage) {
        itemsPerPage = perPage;
    }

    public static int getItemsPerPage() {
        return itemsPerPage;
    }
}
