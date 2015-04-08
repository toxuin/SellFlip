package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import ru.toxuin.sellflip.restapi.ApiService;

public class RemoveAdRequest extends RetrofitSpiceRequest<Void, ApiService> {
    private String adId;

    public RemoveAdRequest(String adId) {
        super(Void.class, ApiService.class);
        this.adId = adId;
    }

    @Override
    public Void loadDataFromNetwork() throws Exception {
        return getService().removeAd(adId);
    }
}
