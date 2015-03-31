package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.restapi.ApiService;

public class CreateAdRequest extends RetrofitSpiceRequest<SingleAd, ApiService> {
    private SingleAd ad;

    public CreateAdRequest(SingleAd ad) {
        super(SingleAd.class, ApiService.class);
        this.ad = ad;
    }

    @Override
    public SingleAd loadDataFromNetwork() throws Exception {
        return getService().createNewAd(ad);
    }
}
