package ru.toxuin.sellflip.restapi;

import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

import retrofit.RestAdapter;

public class AppygramSpiceServices extends RetrofitGsonSpiceService {
    public static final String API_ENDPOINT_URL = "https://arecibo.appygram.com";

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(AppygramApiService.class);
    }

    @Override
    protected String getServerUrl() {
        return API_ENDPOINT_URL;
    }

    @Override
    protected RestAdapter.Builder createRestAdapterBuilder() {

        return new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT_URL);
    }
}
