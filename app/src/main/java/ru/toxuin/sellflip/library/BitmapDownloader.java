package ru.toxuin.sellflip.library;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.library.GridSearchAdapter.SearchResultViewHolder;

import java.io.IOException;
import java.io.InputStream;

public class BitmapDownloader extends AsyncTask<SearchResultViewHolder, Void, Bitmap> {
    private static final String TAG = "BITMAP_LOADER";
    private SearchResultViewHolder viewHolder;

    @Override
    protected Bitmap doInBackground(SearchResultViewHolder... params) {
        viewHolder = params[0];
        if (viewHolder.id == null) return null;
        BitmapCache cache = BitmapCache.getInstance();
        String url = "http://lorempixel.com/512/288/technics/" + viewHolder.id + "/";
        if (cache.getBitmapFromMemCache(url) == null) {
            final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
            final HttpGet getRequest = new HttpGet(url);
            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.e("BITMAP_LOADER", "Error " + statusCode
                            + " while retrieving bitmap from " + url);
                    return null;
                }
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = entity.getContent();
                        Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                        cache.addBitmapToMemoryCache(url, bmp);
                        return bmp;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                client.close();
            }
        } else {
            return cache.getBitmapFromMemCache(url);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (result == null) {
            viewHolder.thumbnail.setImageDrawable(viewHolder.thumbnail.getContext().getResources()
                    .getDrawable(R.drawable.no_image));
        } else {
            viewHolder.thumbnail.setImageBitmap(result);
        }
        Log.d(TAG, "LOADED IMAGE FOR ID " + viewHolder.id);
    }
}


