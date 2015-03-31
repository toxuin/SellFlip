package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.restapi.ApiService;

public class SingleAdRequest extends RetrofitSpiceRequest<SingleAd, ApiService> {
    private String adId;

    public SingleAdRequest(String adId) {
        super(SingleAd.class, ApiService.class);
        this.adId = adId;
    }

    @Override
    public SingleAd loadDataFromNetwork() throws Exception {
        return getService().getSingleAd(adId);
    }

    public String getCacheKey() {
        return "singlead." + adId;
    }

}
