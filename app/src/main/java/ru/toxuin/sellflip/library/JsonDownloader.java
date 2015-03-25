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
import org.json.JSONArray;
import org.json.JSONException;
import ru.toxuin.sellflip.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonDownloader extends AsyncTask<String, Void, JSONArray> {
    private static final String TAG = "JsonDownloader";
    private String serverUrl;

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        if (serverUrl == null) throw new IllegalStateException("Json Downloader was executed without specified server URL. Use .setServerUrl()!");
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(serverUrl);
        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.d(TAG, "ERROR WHILE GETTING JSON FROM " + serverUrl);
                return null;
            }
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    return new JSONArray(sb.toString());
                } catch (JSONException exception) {
                    exception.printStackTrace();
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
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        super.onPostExecute(result);
        if (result == null) return;

        // POPULATE DB

        Log.d(TAG, "DOWNLOADED JSON FROM " + serverUrl);
    }
}
