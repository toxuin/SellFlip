package ru.toxuin.sellflip.restapi;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;
import retrofit.InterceptingClient;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import roboguice.util.temp.Ln;

public class SellFlipSpiceService extends RetrofitGsonSpiceService {
    public static final String API_ENDPOINT_URL = "http://sellflip.me";
    private static ApiHeaders authHeaders = new ApiHeaders();
    private static boolean wipeCache = true;

    @Override
    public void onCreate() {
        super.onCreate();
        if (wipeCache) {
            removeAllDataFromCache();
            wipeCache = false;
        }
        //Ln.getConfig().setLoggingLevel(Log.ERROR);
        addRetrofitInterface(ApiService.class);
    }

    @Override
    protected String getServerUrl() {
        return API_ENDPOINT_URL;
    }

    public static String getEndpointUrl() {
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

    @Override
    public int getThreadCount() {
        return 5;
    }

    public static ApiHeaders getAuthHeaders() {
        return authHeaders;
    }

    public static void clearCache() {
        wipeCache = true;
    }
}
