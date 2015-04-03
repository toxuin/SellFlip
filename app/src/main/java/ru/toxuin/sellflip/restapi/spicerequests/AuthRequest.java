package ru.toxuin.sellflip.restapi.spicerequests;

import com.google.gson.annotations.SerializedName;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import ru.toxuin.sellflip.restapi.ApiService;

public class AuthRequest extends RetrofitSpiceRequest<AuthRequest.AccessToken, ApiService> {
    String facebookToken;

    public AuthRequest(String token) {
        super(AccessToken.class, ApiService.class);
        this.facebookToken = token;
    }

    @Override
    public AccessToken loadDataFromNetwork() throws Exception {
        AccessToken token = new AccessToken();
        token.facebook_token = facebookToken;
        return getService().getAuthToken(token);
    }

    public static class AccessToken {
        @SerializedName("access_token")
        public String facebook_token;
        public String token;
    }
}
