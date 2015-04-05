package ru.toxuin.sellflip.restapi.spicerequests;

import android.content.Context;
import android.graphics.Bitmap;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.simple.BitmapRequest;
import ru.toxuin.sellflip.library.BitmapCache;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class SingleAdThumbRequest extends SpiceRequest<Bitmap> {
    BitmapCache cache;
    Context context;
    String adId;

    public SingleAdThumbRequest(Context context, String id) {
        super(Bitmap.class);
        cache = BitmapCache.getInstance();
        this.context = context;
        adId = id;
    }

    @Override
    public Bitmap loadDataFromNetwork() throws Exception {
        String url = SellFlipSpiceService.getEndpointUrl() + "/api/v1/adsItems/"+ adId + "/thumb";;
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
