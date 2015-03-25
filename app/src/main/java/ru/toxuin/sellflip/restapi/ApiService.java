package ru.toxuin.sellflip.restapi;

import java.util.List;

import org.json.JSONArray;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import ru.toxuin.sellflip.entities.SingleAd;

public interface ApiService {
    public static final String LIST_ADS = "/adsItems";
    public static final String SINGLE_AD = "/adsItems/{id}";
    public static final String VIDEO_UPLOAD = "/adsItems/{id}/upload";
    public static final String CATEGORIES = "/categories";

    @GET(LIST_ADS) void listTopAds(@Query("skip") int skip, @Query("limit") int limit, Callback<List<SingleAd>> callback);
    @GET(SINGLE_AD) void getSingleAd(@Path("id") String id, Callback<SingleAd> callback);
    @GET(CATEGORIES) void getCategories(Callback<JSONArray> callback);
    @POST(LIST_ADS) void createNewAd(@Body SingleAd ad, Callback<SingleAd> callback);

    @Multipart
    @POST(VIDEO_UPLOAD) void uploadVideo(@Part("file") TypedFile file, @Path("id") String id, Callback<Void> callback);
}
