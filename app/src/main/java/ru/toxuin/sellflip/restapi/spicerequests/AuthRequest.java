package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import ru.toxuin.sellflip.restapi.ApiService;

public class AuthRequest extends RetrofitSpiceRequest<AuthRequest.AccessToken, ApiService> {
    String accessToken;

    public AuthRequest(String token) {
        super(AccessToken.class, ApiService.class);
        this.accessToken = token;
    }

    @Override
    public AccessToken loadDataFromNetwork() throws Exception {
        AccessToken token = new AccessToken();
        token.access_token = accessToken;
        return getService().getAuthToken(token);
    }

    public static class AccessToken {
        String access_token;
        String token;
    }
}
