package ru.toxuin.sellflip.restapi;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import ru.toxuin.sellflip.entities.SingleAd;

import java.util.List;

public class ApiConnector {
    public static final String TAG = "API_CONNECTOR";
    public static final String API_ENDPOINT_URL = "http://appfrontend-mavd.rhcloud.com/api";

    ApiService apiService;
    RestAdapter restAdapter;

    private static ApiConnector instance;
    private ApiConnector() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd") // Will complain about default JS format without it
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        apiService = restAdapter.create(ApiService.class);
    }

    public static ApiConnector getInstance() {
        if (instance == null) {
            instance = new ApiConnector();
        }
        return instance;
    }

    public void requestSingleAdForId(String adId, Callback<SingleAd> callback) {
        Log.d(TAG, "REQUESTED AD FOR ID " + adId + " ...");
        apiService.getSingleAd(adId, callback);
    }

    public void requestTopAds(Callback<List<SingleAd>> callback) {
        apiService.listAds(callback);
        Log.d(TAG, "REQUESTING TOP ADS");
    }
}

