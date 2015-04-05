package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import ru.toxuin.sellflip.entities.Like;
import ru.toxuin.sellflip.restapi.ApiService;

public class LikeRequest extends RetrofitSpiceRequest<Like, ApiService> {
    String adId;
    boolean like;

    public LikeRequest(String adId, boolean like) {
        super(Like.class, ApiService.class);
        this.adId = adId;
        this.like = like;
        this.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_DELAY_BEFORE_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public Like loadDataFromNetwork() throws Exception {
        if (like) return getService().likeAd(adId);
        else return getService().unlikeAd(adId);
    }

    public String getCacheKey() {
        return "like." + adId;
    }
}
