package ru.toxuin.sellflip.library;

import android.support.v4.util.LruCache;

import android.graphics.Bitmap;

public class BitmapCache {
    private LruCache<String, Bitmap> memoryCache;
    private static BitmapCache self;

    public static BitmapCache getInstance() {
        if (self == null) {
            self = new BitmapCache();
        }
        return self;
    }

    private BitmapCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }
}
