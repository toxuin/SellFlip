package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import retrofit.mime.TypedFile;
import ru.toxuin.sellflip.restapi.ApiService;

import java.io.File;

public class VideoUploadRequest extends RetrofitSpiceRequest<Void, ApiService> {
    private String adId;
    private String fileName;

    public VideoUploadRequest(String id, String filename) {
        super(Void.class, ApiService.class);
        this.adId = id;
        this.fileName = filename;
        this.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_DELAY_BEFORE_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    public Void loadDataFromNetwork() throws Exception {
        return getService().uploadVideo(new TypedFile("video/mp4", new File(fileName)), adId);
    }
}
