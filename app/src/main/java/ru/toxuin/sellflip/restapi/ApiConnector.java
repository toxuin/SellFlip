package ru.toxuin.sellflip.restapi;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.LocalJsonClient;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedFile;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.entities.SingleAd;

import java.io.File;
import java.util.List;

public class ApiConnector {
    public static final String TAG = "API_CONNECTOR";
    public static final String API_ENDPOINT_URL = "http://appfrontend-mavd.rhcloud.com/api/v1";
    private static int itemsOnPage = 7;

    ApiService apiService;
    RestAdapter restAdapter;

    ApiService cachedApi;
    RestAdapter cachedRestAdapter;

    private static ApiConnector instance;
    private ApiConnector(Context ctx) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd") // Will complain about default JS format without it
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        apiService = restAdapter.create(ApiService.class);

        cachedRestAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT_URL)
                .setClient(new LocalJsonClient(ctx))
                .setConverter(new GsonConverter(gson))
                .build();

        cachedApi = cachedRestAdapter.create(ApiService.class);
    }

    public static ApiConnector getInstance(Context ctx) {
        if (instance == null) {
            instance = new ApiConnector(ctx);
        }
        return instance;
    }

    public void requestSingleAdForId(String adId, Callback<SingleAd> callback) {
        Log.d(TAG, "REQUESTED AD FOR ID " + adId + " ...");
        apiService.getSingleAd(adId, callback);
    }

    public void requestTopAdsPaged(int page, Callback<List<SingleAd>> callback) {
        int skip = getItemsOnPage() * page;
        int limit = getItemsOnPage();
        Log.d(TAG, "REQUESTING TOP ADS FROM " + skip + " TO " + (skip+limit) + "(" + limit + " ITEMS)");
        apiService.listTopAds(skip, limit, callback);
    }

    public void createNewAd(SingleAd ad, Callback<SingleAd> callback) {
        Log.d(TAG, "POSTING NEW AD");
        apiService.createNewAd(ad, callback);
    }

    public void requestCategories(Callback<List<Category>> callback) {
        Log.d(TAG, "REQUESTING CATEGORIES...");
        cachedApi.getCategories(callback);
    }

    public void uploadVideo(String id, String filename, Callback<Void> callback) {
        apiService.uploadVideo(new TypedFile("video/mp4", new File(filename)), id, callback);
        Log.d(TAG, "UPLOADING VIDEO...");
    }



    // GETTERS AND SETTERS

    public static int getItemsOnPage() {
        return itemsOnPage;
    }

    public static void setItemsOnPage(int itemsOnPage) {
        ApiConnector.itemsOnPage = itemsOnPage;
    }
}

