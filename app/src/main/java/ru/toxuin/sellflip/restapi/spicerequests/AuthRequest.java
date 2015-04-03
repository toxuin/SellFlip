package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.octo.android.robospice.retry.DefaultRetryPolicy;

import ru.toxuin.sellflip.restapi.ApiService;

public class AuthRequest extends RetrofitSpiceRequest<AuthRequest.AccessToken, ApiService> {
    String facebookToken;

    public AuthRequest(String facebookToken) {
        super(AccessToken.class, ApiService.class);
        this.facebookToken = facebookToken;
        this.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_DELAY_BEFORE_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public AccessToken loadDataFromNetwork() throws Exception {
        AccessToken token = new AccessToken();
        token.access_token = facebookToken;
        return getService().getAuthToken(token);
    }

    public static class AccessToken {
        public String access_token; // fb token passed to back end
        public String token; // back end auth token
    }
}
