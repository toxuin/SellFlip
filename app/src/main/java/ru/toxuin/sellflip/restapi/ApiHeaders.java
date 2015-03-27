package ru.toxuin.sellflip.restapi;

import retrofit.RequestInterceptor;

public class ApiHeaders implements RequestInterceptor {
    private String accessToken;

    public void clearToken() {
        this.accessToken = null;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    @Override public void intercept(RequestFacade request) {
        String authValue = this.accessToken;
        if (authValue != null) {
            request.addHeader("Authorization", "Bearer " + authValue);
        }
    }
}
