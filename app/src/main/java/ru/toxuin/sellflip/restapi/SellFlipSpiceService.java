package ru.toxuin.sellflip.restapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;
import retrofit.InterceptingClient;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import ru.toxuin.sellflip.entities.Category;

import java.util.Date;

public class SellFlipSpiceService extends RetrofitGsonSpiceService {
    public static final String API_ENDPOINT_URL = "https://sellflip.me";
    private static final String MEDIA_ENDPOINT_URL = "http://sellflip.me";
    private static boolean wipeCache = true;

    public static String getEndpointUrl() {
        return API_ENDPOINT_URL;
    }

    public static void clearCache() {
        wipeCache = true;
    }

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

    @Override
    protected RestAdapter.Builder createRestAdapterBuilder() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(Category.List.class, new CategoryDeserializer())
                .create();

        return new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT_URL)
                .setConverter(new GsonConverter(gson))
                .setClient(new InterceptingClient(getApplicationContext()))
                .setRequestInterceptor(ApiHeaders.getInstance());
    }

    @Override
    public int getThreadCount() {
        return 5;
    }

    public static String getMediaEndpointUrl() {
        return MEDIA_ENDPOINT_URL;
    }
}
