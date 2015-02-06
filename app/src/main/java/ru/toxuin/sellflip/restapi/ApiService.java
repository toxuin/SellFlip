package ru.toxuin.sellflip.restapi;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import ru.toxuin.sellflip.entities.SingleAd;

public interface ApiService {
    public static final String LIST_ADS = "/adsItems";
    public static final String SINGLE_AD = "/adsItems/{id}";

    @GET(LIST_ADS) void listTopAds(@Query("skip") int skip, @Query("limit") int limit, Callback<List<SingleAd>> callback);
    @GET(SINGLE_AD) void getSingleAd(@Path("id") String id, Callback<SingleAd> callback);
}
