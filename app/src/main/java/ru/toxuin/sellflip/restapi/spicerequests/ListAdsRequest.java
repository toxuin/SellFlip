package ru.toxuin.sellflip.restapi.spicerequests;

import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.restapi.ApiService;

public class ListAdsRequest extends RetrofitSpiceRequest<SingleAd.List, ApiService> {
    private static final String TAG = "LIST_ADS_REQUEST";
    private static int itemsPerPage = 7;
    private String category;
    private String searchTerm;
    private int page;
    private String order;
    private boolean listMy;

    public ListAdsRequest(String category, String searchTerm, String order, int page) {
        super(SingleAd.List.class, ApiService.class);
        this.searchTerm = searchTerm;
        this.category = category;
        this.order = order;
        this.page = page;
        this.listMy = false;
    }

    public static int getItemsPerPage() {
        return itemsPerPage;
    }

    public static void setItemsPerPage(int perPage) {
        itemsPerPage = perPage;
    }

    @Override
    public SingleAd.List loadDataFromNetwork() throws Exception {
        int skip = itemsPerPage * page;
        int limit = itemsPerPage;
        Log.d(TAG, "REQUESTING TOP ADS FROM " + skip + " TO " + (skip + limit) + "(" + limit + " ITEMS)");
        if (listMy) return getService().getMyAds(category, searchTerm, order, skip, limit);
        else return getService().listTopAds(category, searchTerm, order, skip, limit);

    }

    public String getCacheKey() {
        return "getTopAds." + category + "." + page + "." + order + "." + (listMy?"onlymine":"everything");
    }

    public void setOnlyMine(boolean onlyMine) {
        listMy = onlyMine;
    }
}
