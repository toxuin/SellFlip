package ru.toxuin.sellflip.restapi;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthRequestTask extends AsyncTask<String, String, String> {
    public static final String TAG = "AUTH_TASK";

    private static final String AUTH_URL = "http://appfrontend-mavd.rhcloud.com/auth/facebook_token";
    private AuthResponseListener listener;

    public AuthRequestTask registerResponseListener(AuthResponseListener listener) {
        this.listener = listener;
        return this;
    }

    @Override protected String doInBackground(String... params) {
        // params[0] - token
        String secure_token = null;

        try {
            URL url = new URL(AUTH_URL);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(6000);
            httpURLConnection.setReadTimeout(6000);
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONObject auth = new JSONObject();
            auth.put("access_token", params[0]);

            OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream());
            wr.write(auth.toString());
            wr.close();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Got OK for authorization");
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                JSONObject json = new JSONObject(responseStrBuilder.toString());
                secure_token = json.get("token").toString();
                inputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return secure_token;
    }

    @Override protected void onPostExecute(String secure_token) {
        if (secure_token != null) {
            ApiConnector.getAuthHeaders().setAccessToken(secure_token);
            listener.onAuthSuccess();
        } else {
            listener.onAuthFailure();
        }
    }
}
