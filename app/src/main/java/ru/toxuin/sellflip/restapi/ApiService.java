package ru.toxuin.sellflip.restapi;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.entities.Like;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.entities.User;
import ru.toxuin.sellflip.restapi.spicerequests.AuthRequest;

public interface ApiService {
    String LIST_ADS = "/api/v1/adsItems";
    String MY_ADS = "/api/v1/adsItems/myAds/";
    String SINGLE_AD = "/api/v1/adsItems/{id}";
    String VIDEO_UPLOAD = "/api/v1/adsItems/{id}/upload";

    String CATEGORIES = "/api/v1/categories";
    String AUTH = "/auth/facebook_token";

    String CURRENT_USER = "/api/v1/users/me";

    @GET(LIST_ADS) SingleAd.List listTopAds(@Query("category") String category, @Query("search") String searchTerm, @Query("order") String order, @Query("skip") int skip, @Query("limit") int limit);
    @GET(SINGLE_AD) SingleAd getSingleAd(@Path("id") String id);

    @GET(MY_ADS) SingleAd.List getMyAds(@Query("category") String category, @Query("search") String searchTerm, @Query("order") String order, @Query("skip") int skip, @Query("limit") int limit);
    @POST(LIST_ADS) SingleAd createNewAd(@Body SingleAd ad);
    @GET(CATEGORIES) Category.List getCategories();
    @POST(AUTH) AuthRequest.AccessToken getAuthToken(@Body AuthRequest.AccessToken access_token);

    @DELETE(SINGLE_AD) Void removeAd(@Path("id") String id);
    @POST(SINGLE_AD + "/like") Like likeAd(@Path("id") String id);
    @POST(SINGLE_AD + "/unlike") Like unlikeAd(@Path("id") String adId);

    @GET(CURRENT_USER) User getCurrentUser();

    @Multipart
    @POST(VIDEO_UPLOAD) Void uploadVideo(@Part("file") TypedFile file, @Path("id") String id);
}
