package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import ru.toxuin.sellflip.restapi.ApiService;

public class VideoUploadRequest extends RetrofitSpiceRequest<Void, ApiService> {
    private String adId;
    private String fileName;

    public VideoUploadRequest(String id, String filename) {
        super(Void.class, ApiService.class);
        this.adId = id;
        this.fileName = filename;
    }

    @Override
    public Void loadDataFromNetwork() throws Exception {
        return null;
    }
}
