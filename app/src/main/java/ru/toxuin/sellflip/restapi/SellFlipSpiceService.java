package ru.toxuin.sellflip.restapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;
import retrofit.InterceptingClient;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class SellFlipSpiceService extends RetrofitGsonSpiceService {
    public static final String API_ENDPOINT_URL = "http://appfrontend-mavd.rhcloud.com";
    private static ApiHeaders authHeaders = new ApiHeaders();

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(ApiService.class);
    }

    @Override
    protected String getServerUrl() {
        return API_ENDPOINT_URL;
    }

    @Override
    protected RestAdapter.Builder createRestAdapterBuilder() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd") // Will complain about default JS format without it
                .create();

        return new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT_URL)
                .setConverter(new GsonConverter(gson))
                .setClient(new InterceptingClient(getApplicationContext()))
                .setRequestInterceptor(authHeaders);
    }

    public static ApiHeaders getAuthHeaders() {
        return authHeaders;
    }
}
