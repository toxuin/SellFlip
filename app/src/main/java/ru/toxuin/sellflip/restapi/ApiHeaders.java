package ru.toxuin.sellflip.restapi;

import retrofit.RequestInterceptor;

public class ApiHeaders implements RequestInterceptor {
    private static String backEndAccessToken;
    private static ApiHeaders instance = new ApiHeaders();

    private ApiHeaders() {}

    public static ApiHeaders getInstance() {
        return instance;
    }

    public static boolean isTokenEmpty() {
        return backEndAccessToken != null && backEndAccessToken.isEmpty();
    }

    public static void clearToken() {
        backEndAccessToken = null;
    }

    public static String getAccessToken() {
        return backEndAccessToken;
    }

    public static void setAccessToken(String token) {
        backEndAccessToken = token;
    }

    @Override public void intercept(RequestFacade request) {
        if (backEndAccessToken != null) {
            request.addHeader("Authorization", "Bearer " + backEndAccessToken);
        }
    }
}
