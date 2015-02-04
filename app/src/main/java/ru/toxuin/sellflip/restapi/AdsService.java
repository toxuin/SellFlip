package ru.toxuin.sellflip.restapi;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import ru.toxuin.sellflip.entities.SingleAd;

public interface AdsService {
    public static final String AD_API_ENDPOINT_LINK = "/adsItems";

    @GET(AD_API_ENDPOINT_LINK) void listAds(Callback<List<SingleAd>> cb);
}
