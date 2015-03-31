package ru.toxuin.sellflip.restapi;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.entities.SingleAd;

public interface ApiService {
    String LIST_ADS = "/adsItems";
    String SINGLE_AD = "/adsItems/{id}";
    String VIDEO_UPLOAD = "/adsItems/{id}/upload";
    String CATEGORIES = "/categories";

    @GET(LIST_ADS) void listTopAds(@Query("category") String category, @Query("skip") int skip, @Query("limit") int limit, Callback<List<SingleAd>> callback);
    @GET(LIST_ADS) SingleAd.List listTopAds(@Query("category") String category, @Query("skip") int skip, @Query("limit") int limit);

    @GET(SINGLE_AD) void getSingleAd(@Path("id") String id, Callback<SingleAd> callback);
    @GET(SINGLE_AD) SingleAd getSingleAd(@Path("id") String id);

    @POST(LIST_ADS) void createNewAd(@Body SingleAd ad, Callback<SingleAd> callback);
    @POST(LIST_ADS) SingleAd createNewAd(@Body SingleAd ad);

    @GET(CATEGORIES) void getCategories(Callback<List<Category>> callback);
    @GET(CATEGORIES) Category.List getCategories();

    @Multipart
    @POST(VIDEO_UPLOAD) void uploadVideo(@Part("file") TypedFile file, @Path("id") String id, Callback<Void> callback);
}
