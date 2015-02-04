package ru.toxuin.sellflip.restapi;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import ru.toxuin.sellflip.entities.SingleAd;

public interface ApiService {
    public static final String LIST_ADS = "/adsItems";
    public static final String SINGLE_AD = "/adsItems/{id}";

    @GET(LIST_ADS) void listAds(Callback<List<SingleAd>> cb);
    @GET(SINGLE_AD) void getSingleAd(@Path("id") String id, Callback<SingleAd> callback);
}
