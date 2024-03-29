package ru.toxuin.sellflip.restapi.spicerequests;

import android.content.Context;
import android.graphics.Bitmap;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.simple.BitmapRequest;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import ru.toxuin.sellflip.library.BitmapCache;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class SingleAdThumbRequest extends SpiceRequest<Bitmap> {
    BitmapCache cache;
    Context context;
    String adId;
    int width = -1;

    public SingleAdThumbRequest(Context context, String id) {
        this(context, id, -1);
    }

    public SingleAdThumbRequest(Context context, String id, int width) {
        super(Bitmap.class);
        this.width = width;
        cache = BitmapCache.getInstance();
        this.context = context;
        adId = id;
    }

    @Override
    public Bitmap loadDataFromNetwork() throws Exception {
        String url = SellFlipSpiceService.getMediaEndpointUrl() + "/api/v1/publicAdsItems/"+ adId + "/thumb";
        if (width > 0) url += "/" + width;
        if (cache.getBitmapFromMemCache(url) == null) {

            File parentDir = new File(context.getCacheDir() + File.pathSeparator + "tmp_item_bitmaps");
            parentDir.mkdirs();
            File cacheFile = new File(parentDir, ""+UUID.randomUUID());
            if (cacheFile.exists()) cacheFile.delete();
            cacheFile.createNewFile();
            BitmapRequest request = new BitmapRequest(url, cacheFile);
            Bitmap bmp;
            bmp = request.loadDataFromNetwork();
            if (bmp != null) cache.addBitmapToMemoryCache(url, bmp);
            return bmp;
        } else return cache.getBitmapFromMemCache(url);
    }
}
